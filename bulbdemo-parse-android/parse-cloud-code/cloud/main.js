
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("sendNotification", function(request, response) {
	var data1 = request.params.data;
	var query = new Parse.Query(Parse.Installation);
	Parse.Push.send({
        where: query,
        data:{ alert:""+data1}
    }, {
    success: function() {
        // Push was successful
		response.success("Push Successful");
    },
    error: function(error) {
        // Handle error
		response.success("Push Failure" );
    }
    });
  
});
