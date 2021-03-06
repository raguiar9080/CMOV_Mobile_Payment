var sqlite3 = require("sqlite3").verbose();
var moment = require('moment');
var fs = require("fs");

ticketConn = undefined;


module.exports.db=sqliteDB;
module.exports.Client=Client;

/*
 *	CLIENT CLASS
 */ 
 
 function Client(name){
 	this.id=null;
 	this.name=name;
 	this.tickets=[];
 	this.nib=null;
 	this.cardType=null;
 	this.validity=null;
 }

/*
 *	BUS CLASS
 */ 
 
 function Bus(name){
 	this.id=null;
 	this.description=description;
 }

/*
 *	TICKET CLASS
 */ 
 
 function Ticket(name){
 	this.id=null;
 	this.type=null;
 	this.duration=null;
 }

 //get time
 function timeNow()
 {
 	return ( new Date() / 1000 ) | 0 ;
 }

 function timestamp(){
 	return moment().format("YYYY-MM-DDTHH:mm:ss");
 }

/*
 *	DATABASE
 */
 
 function sqliteDB(file) 
 {

 	console.log("Opening Database:"+file);
 	var exists = fs.existsSync(file);

 	if( !exists )
 	{
 		console.log("File not found. Creating new Database file: " + file );
 		fs.openSync(file, "w");
 	}

 	ticketConn = new sqlite3.Database(file,function() {
 		ticketConn.run('PRAGMA foreign_keys=on');
 	});

 	if( !exists )
 	{
		// Serialize forces the order of the operations, nothing is parallel
		ticketConn.serialize(function(){

			ticketConn
			.run("BEGIN;")

				// Create tables
				.run("CREATE TABLE clients (cid INTEGER PRIMARY KEY, name TEXT, nib TEXT NOT NULL, cardType TEXT NOT NULL, validity TEXT NOT NULL, pass TEXT NOT NULL, UNIQUE(name));")
				.run("CREATE TABLE tickets (tid INTEGER PRIMARY KEY, type TEXT, duration INTEGER NOT NULL);")
				.run("CREATE TABLE buses (bid INTEGER PRIMARY KEY, description TEXT);")

				.run("CREATE TABLE transactions (id INTEGER PRIMARY KEY, cid REFERENCES clients(cid), tid REFERENCES tickets(tid), bid REFERENCES buses(bid), dateValidated TEXT, dateBought TEXT NOT NULL);")
				
				// Insert first data
				.run("INSERT INTO clients (name, nib, cardType, validity, pass) VALUES ('ADMIN', 'NIB', 'CARDTYPE', 'VALIDITY', 'PASWORD') , ('Ruben', 'nib123', 'card123', 'validity123', 'passwordencrypted');")
				.run("INSERT INTO tickets (type, duration) VALUES ('T1', 15), ('T2', 30), ('T3', 60);")
				.run("INSERT INTO buses (description) VALUES ('Porto-Gaia'), ('Lisboa-Porto');")
				.run("INSERT INTO transactions (cid, tid, dateBought) VALUES (1, 1, 'DATE');")
				.run("COMMIT;");
			});
}
}

sqliteDB.prototype.createClient=function(client, callback)
{
	console.log("adding client to db ", client.name);
	if( typeof callback !== 'function')
		throw new Error('Callback is not a function');
	ticketConn.run("INSERT INTO clients (name, nib, cardType, validity,pass) VALUES (?, ?, ?, ?, ?);",
		[client.name, client.nib, client.cardType, client.validity, client.pass],
		function(err){
			callback(err, this.lastID, this.changes);
		});
}

sqliteDB.prototype.login=function(name, pass, callback)
{
	console.log("login client: ", name);	
	if( typeof callback !== 'function')
		throw new Error('Callback is not a function');
	ticketConn.all("SELECT * FROM clients WHERE name=?",
		[name],
		function(err, row) {
			if( row && row.length > 0 )
			{
				row=row[0];
				if (row.pass == pass)callback(err,row );
				else callback(err,null);
			}
			else
				callback(err,null );
		});
}

sqliteDB.prototype.validate=function(cid, tid, bid, callback)
{
	console.log("Validating ticket:_" + tid + "_; client:_" + cid + "_;bus:_" + bid + "_");
	console.log(typeof(tid) + typeof(cid));
	if ( typeof callback !== 'function')
		throw new Error('Callback is not a function');
	ticketConn.get("SELECT * FROM transactions WHERE tid = ? AND cid = ? AND bid IS NULL",
		[tid, cid],
		function (err, row) {
			if (row)
			{
				var date=timestamp();
				var transaction_id=row.id;
				console.log("id encontrado:_" + transaction_id + "_");
				ticketConn.run("UPDATE transactions SET dateValidated=?, bid=? WHERE id=?",[date,bid,transaction_id],
					function(err)
					{
						if (!err)console.log("Sucess on Validation",transaction_id);
						else console.log("DB error on Validation");
						callback(err, transaction_id);
					});
			}
			else
				callback(err,null);
		});
}

sqliteDB.prototype.listTickets=function(clientID,callback)
{
	console.log("list tickets for ",clientID);
	if ( typeof callback !== 'function')
		throw new Error('Callback is not a function');

	ticketConn.all("SELECT transactions.id, transactions.tid, type, duration FROM transactions, tickets WHERE transactions.tid = tickets.tid AND transactions.cid = ? AND transactions.bid IS NULL",
		[clientID],
		function (err,row) { //eachfunction
			console.log(err + " --- " + row);
			if( row && row.length > 0 )
			{
				callback(err,row);
			}
			else
				callback(err,null);
		});	
}


sqliteDB.prototype.prepareBuyTickets=function(t1,t2,t3,callback)
{
	console.log("preparing buy tickets");
	if ( typeof callback !== 'function')
		throw new Error('Callback is not a function');	
	var count = t1 + t2 + t3;
	var out = {};
	out.price = t1 + 2*t2 + 3*t3;

	//adding bonus
	if (count >= 10)
	{
		if(t1 > 0 )
			t1++;
		else if (t2 > 0)
			t2++;
		else
			t3++;
	}
	
	out.t1 = t1;
	out.t2 = t2;
	out.t3 = t3;
	callback(null,out);
}

sqliteDB.prototype.buyTickets=function(clientID,t1,t2,t3,callback)
{
	console.log("buy tickets for ",clientID);
	if ( typeof callback !== 'function')
		throw new Error('Callback is not a function');	
	var count = t1 + t2 + t3;
	var out = {};
	out.price = t1 + 2*t2 + 3*t3;

	//adding bonus
	if (count >= 10)
	{
		if(t1 > 0 )
			t1++;
		else if (t2 > 0)
			t2++;
		else
			t3++;
	}
	var ts=timestamp();
	
	ticketConn.parallelize(function(err,row){
		for (var i=0;i<t3;i++)
		{
			ticketConn.run("INSERT INTO transactions (cid,tid, dateBought) VALUES (?,3,?);",[clientID,ts]);
		}
		for (var j=0;j<t2;j++)
		{
			ticketConn.run("INSERT INTO transactions (cid,tid, dateBought) VALUES (?,2,?);",[clientID,ts]);

		}
		for (var k=0;k<t1;k++)
		{
			ticketConn.run("INSERT INTO transactions (cid,tid, dateBought) VALUES (?,1,?);",[clientID,ts]);

		}
	});
	out.t1 = t1;
	out.t2 = t2;
	out.t3 = t3;
	callback(null,out);
}

sqliteDB.prototype.getValidatedTickets=function(busId,callback)
{
	if ( typeof callback !== 'function')
		throw new Error('Callback is not a function');	
	
	console.log("get validated for ",busId);
	var out=[];
	var time=moment().subtract('minutes',90).format("YYYY-MM-DDTHH:mm:ss");

	ticketConn.all("SELECT id,tid,cid,dateValidated FROM transactions WHERE (tid=1 OR tid=2 OR tid=3) AND bid=? AND dateValidated<?"
		,[busId,time],
		function (err,row) { //eachfunction
			console.log(err + " --- " + row);
			if( row && row.length > 0 )
			{
				callback(err,row);
			}
			else
				callback(err,null);
		});	
}