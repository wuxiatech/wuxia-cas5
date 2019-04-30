var valid;
var CheckoutForm = function() {
	return {
		// Checkout Form
		initCheckoutForm : function() {
			// Validation
			valid = $('#sky-form').validate({
				debug : false,
				errorPlacement: function(error, element) {
					element.closest("div.table_border_idx").next("em").html(error[0].innerHTML);
					element.closest("div.table_border_idx").next("em").show();
				},
				success: function(label,element) {
					//element.nextAll("em").addClass("rz-error");
				},
				// Rules for form validation
				rules : {
					"username" : {
						required : true
					},
					"password" : {
						required : true,
						minlength : 6,
						maxlength : 16
					}
				},
				// Messages for form validation
				messages : {
					"username" : {
						required : "请输入账号"
					},
					"password" : {
						required : "请输入您的密码",
						minlength : "密码长度不能少于6位",
						maxlength : "密码长度不能大于16位"
					}
				}
			});
		}

	};

}();


$("#sky-form").submit(function(){
	return valid.valid();
});