module.exports = function (app, addon) {

    // Root route. This route will serve the `atlassian-connect.json` unless the
    // documentation url inside `atlassian-connect.json` is set
    app.get('/', function (req, res) {
        res.format({
            // If the request content-type is text-html, it will decide which to serve up
            'text/html': function () {
                res.redirect('/atlassian-connect.json');
            },
            // This logic is here to make sure that the `atlassian-connect.json` is always
            // served up when requested by the host
            'application/json': function () {
                res.redirect('/atlassian-connect.json');
            }
        });
    });

    // This is an example route that's used by the default "generalPage" module.
    // Verify that the incoming request is authenticated with Atlassian Connect
    app.get('/issues-in-project', addon.authenticate(), function (req, res) {
            // Rendering a template is easy; the `render()` method takes two params: name of template
            // and a json object to pass the context in

            res.render('issues-in-project', {
                dashboard: req.query['dashboard'],
                dashboardItem: req.query['dashboardItem']
            });
        }
    );

    app.get('/thumbnail', function(req, res) {
        res.sendfile('public/thumbnail.png');
    });

    app.get('/condition', addon.authenticate(), function(req, res) {
        var view = req.query['view'];
        var dashboard =  req.query['dashboard'];
        var dashboardItem = req.query['dashboardItem'];
        console.log(view);
        console.log(dashboard);
        console.log(dashboardItem);
        res.status(200).send({shouldDisplay: true});
    });

    // Add any additional route handlers you need for views or REST resources here...

};
