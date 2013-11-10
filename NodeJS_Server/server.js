	var express = require('express');
	var app = express();
	var	dbLib = require('./ticketsdatabase');
	var	db = new dbLib.db('ticketsdatabase.db');
	var	port = 81;	// Default

	//configuration
	app.configure(function() {
		app.use(express.bodyParser());
		app.use(function (req, res, next) {
			res.setHeader('Server', 'MobilePayment');

			return next();
		});

		app.enable('trust proxy');
		app.disable('x-powered-by');

		app.use('/', app.router);
	});

	var args = process.argv.splice(2),
	p = null;
	if (args.length > 0) {
		p = parseInt(args[0]);
		if( p )
			port = p ;

		p = null;
	}
	console.log('Listening on port: ' + port)
	app.listen(port);


	/*
	FUNCTIONS
	*/

	function respondToJSON(req, res, out, statusCode) {
		var size;

		out = JSON.stringify( out );
		size = Buffer.byteLength( out, 'UTF-8' );

		res.writeHead( statusCode,
			{ 'Content-Type': 'application/json; charset=utf-8',
			'Content-Length': size} );

		res.write( out );
		res.end();
	}

	/*
	ROUTES
	*/

	app.get('/', function(req, res) {
		res.send('MAIN PAGE');
	});

	//CREATE A CLIENT
	//POST PARAMS: name string, nib string, pass string, cardType string, validity string
	//RETURN JSON {name:<clientname>,id:<clientid>}
	app.post('/client/create',function (req,res) {
		console.log('Method: ' + req.path + " [" + req.method + "]");
		
		var client = new dbLib.Client(req.body.name);
		if( !req.body.name || !req.body.nib ||  !req.body.cardType || !req.body.validity ||!req.body.pass)
		{
			var out = {};
			out.error = "Bad request";
			respondToJSON( req, res, out, 400 );
			
		}
		else
		{
			client.nib = req.body.nib;
			client.cardType=req.body.cardType;
			client.validity=req.body.validity;
			client.pass = req.body.pass;
			db.createClient(client, function(err, lastID, row) {
				var out = {},
				code;

				if( err )
				{
					code = 500;
					out.id = -1;
					out.error = 'Impossible to add client';

					console.log('Error adding client: ' + err);
				}
				else
				{
					code = 200;
					out.id = lastID;
					out.name=client.name;
					console.log('Added new client: ' + lastID + ' ' + client.name);
				}

				respondToJSON( req, res, out, code );

			});
		}
	});

	//LOG ACCOUNT
	//POST PARAMS: name string,pass string
	//RETURN JSON {id:<clientid>}
	app.post('/client/login',function (req,res) {
		console.log('Method: ' + req.path + " [" + req.method + "]");
		
		if( !req.body.name ||!req.body.pass)
			respondToJSON( req, res, {error: 'Bad request'}, 400 );
		else
		{
			var name=req.body.name,pass=req.body.pass;
			db.login(name,pass, function(err,row) {
				var out = {},
				code;

				if( err ) {
					code = 500;
					out.id = -1;
					out.error = 'Impossible to find client. Possible DB Error.';

					console.log('Error login client: ' + err);
				}
				else {
					code = 200;
					if (!row)
					{
						out.id = -1;
						out.error = 'Wrong user or password';
						
						console.log('Fail login');
					}
					else
					{

						out.id=row.cid;
						console.log('Logged in : ' +name);
					}
				}

				respondToJSON( req, res, out, code );

			});
		}

	});

	//VALIDATE TICKET
	//POST /validate PARAMS: cid:client id, type:ticket type, bid:bus id
	//Return JSON {status:true/false}
	app.post('/validate',function(req,res){
		if( !req.body.tid || !req.body.cid || !req.body.bid)
			respondToJSON( req, res, {error: 'Bad request'}, 400 );
		else
		{
			var cid=Number(req.body.cid);
			var tid=Number(req.body.tid);
			var bid=Number(req.body.bid);

			db.validate(cid,tid,bid, function(err,row) {
				var out = {},
				code;

				if( err ) {
					code = 500;
					out.error = 'Impossible to validate ticket';
					out.status=false;
					console.log('Error validating ticket: ' + err);
				}
				else {
					code = 200;
					if (!row)
					{
						out.error = 'Wrong PARAMS';
						out.status=false;
						console.log('Fail validate: ',tid,' ',cid,' ',bid);
					}
					else
					{
						out.status = true;
						out.key = row;
						console.log('validate ticket: ',tid,' ',cid,' ',bid);
					}
				}

				respondToJSON( req, res, out, code );

			});
		}
	});

	// LIST TICKETS
	// GET /list/:clientID PARAMS clientID: numeric client ID
	//returns {t1:21,t2:32,t3:43}
	app.post('/listTickets', function (req, res) {
		if( !req.body.cid)
			respondToJSON( req, res, {error: 'Bad request'}, 400 );
		else
		{
			var cid=Number(req.body.cid);

			db.listTickets(cid, function(err,row) {
				var out = {},
				code;

				if( err ) {
					code = 500;
					out.error = 'Impossible to list tickest';
					out.status=false;
					console.log('Error listing tickets: ' + err);
				}
				else {
					code = 200;
					if (!row)
					{
						out.status=[];
						console.log('Fail listing: ',cid);
					}
					else
					{
						out.status=row;
						console.log('listing ticket: ',cid);
					}
				}

				respondToJSON( req, res, out, code );

			});
		}
	});


	// PREPARE BUY TICKETS
	// POST /buy PARAMS: cid:client id, t1:nr de t1s, t2:nr de t2s,t3:nr de t3s
	// returns {t1:21,t2:32,t3:43,price:100}
	app.post('/prepareBuyTickets', function (req, res) {
		console.log("Preparing Buyng tickets:_t1_" + req.body.t1 + "_t2:_" + req.body.t2 + "_t3:_" + req.body.t3 + "_");
		if( !req.body.t1||!req.body.t2||!req.body.t3)
			respondToJSON( req, res, {error: 'Bad request'}, 400 );
		else {
			var t1=Number(req.body.t1),
				t2=Number(req.body.t2),
				t3=Number(req.body.t3);
			console.log("DATABASE");
			db.prepareBuyTickets(t1,t2,t3, function(err,row) {
				var code;
				var out = {};

				if( err ) {
					code = 500;
					out.error = 'Impossible to buys tickets for client';
					console.log('Error listing tickets: ' + err);
				}
				else {
					code = 200;
					if (!row)
					{
						out.error = 'Wrong PARAMS';
						console.log('Fail prepare buy tickets: ',cid);
					}
					else
					{
						out.status = "OK";
						out.info = row;
						console.log('prepared buy: ' + JSON.stringify( row ));
					}
				}
				respondToJSON( req, res, out, code );
			});
			
		}
	});

	// BUY TICKETS
	// POST /buy PARAMS: cid:client id, t1:nr de t1s, t2:nr de t2s,t3:nr de t3s
	// returns {null}
	app.post('/buyTickets', function (req, res) {
		console.log('Buyng tickets for:' + req.body.cid + "t1:" + req.body.t1 + "t2:" + req.body.t2 + "t3:" + req.body.t3);
		
		if( !req.body.cid||!req.body.t1||!req.body.t2||!req.body.t3)
			respondToJSON( req, res, {error: 'Bad request'}, 400 );

		else {
			var cid = Number(req.body.cid),
			t1=Number(req.body.t1),
			t2=Number(req.body.t2),
			t3=Number(req.body.t3);
			db.buyTickets(cid,t1,t2,t3, function(err,row) {
				var code;
				var out = {};

				if( err ) {
					code = 500;
					out.error = 'Impossible to buys tickets for client';
					console.log('Error listing tickets: ' + err);
				}
				else {
					code = 200;
					if (!row)
					{
						out.error = 'Wrong PARAMS';
						console.log('Fail buy tickets: ',cid);
					}
					else
					{
						out.status = "OK";
						out.info = row;
						console.log('TICKETS BOUGHT: ' + JSON.stringify( row ));
					}
				}

				respondToJSON( req, res, out, code );

			});
			
		}
	});

	//verificar bilhetes validados num terminal
	//GET /validated/:busId PARAMS busId=id do terminal de validacao
	//returns json array client IDs [1,2,4]
	app.post("/getValidatedTickets",function(req,res){
		var bid=Number(req.body.bid);
		if (!bid)
			respondToJSON( req, res, {error: 'Bad request'}, 400 );
		else
		{
			db.getValidatedTickets(bid, function(err,row) {
				var out = {},
				code;

				if( err ) {
					code = 500;
					out.error = 'Impossible to list validated tickest';
					out.status=false;
					console.log('Error listing validated tickets: ' + err);
				}
				else {
					code = 200;
					if (!row)
					{
						out.status=[];
						console.log('Fail listing validated: ',bid);
					}
					else
					{
						out.status=row;
						console.log('listing validated ticket: ',bid);
					}
				}

				respondToJSON( req, res, out, code );

			});
		}
	});

	app.all('*', function (req, res) {

		console.log('Pedido não encontrado: ' + req.path + " [" + req.method + "]");

		respondToJSON( req, res, { error: 'Página não encontrada'}, 404 );
	});