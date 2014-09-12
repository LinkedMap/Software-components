/* ==========================================================
 * lms.js 
 * ========================================================== */

/**
 * Start with helper
 */
String.prototype.startsWith = function(string) {
	return (this.indexOf(string) === 0);
};

/**
 * Core logic
 */
$(function() {
	var frm = $('.form');
	frm.submit(function(e) {
		e.preventDefault();

		var $inputs = $(this).find(':input');
		var values = {};
		$inputs.each(function() {
			values[this.name] = $(this).val();
		});

		var url = values["path"].substring(1);
		for ( var key in values) {
			if (values.hasOwnProperty(key) && values[key].length > 0) {
				url = url.replace('{' + key + '}', values[key])
			}
		}
		var formOk = url.indexOf('{') === -1;

		if (formOk) {
			$.ajax({
				url : url,
				headers : {
					Accept : values["responseContentType"]
				},
				cache : true,
				success : function(data, textStatus, response) {
					var i = $('#' + values['info']).removeClass().addClass(
							'panel panel-success');
					$(i).find('.panel-heading').empty().append(
							'<span>'
									+ response.statusCode().status
									+ ' '
									+ textStatus
									+ ' ('
									+ response
											.getResponseHeader('Content-Type')
									+ ')</span>');
					var str2DOMElement = function(html) {
						var frame = document.createElement('iframe');
						frame.style.display = 'none';
						document.body.appendChild(frame);
						frame.contentDocument.open();
						frame.contentDocument.write(html);
						frame.contentDocument.close();
						var el = frame.contentDocument;
						document.body.removeChild(frame);
						return el;
					};

					$('#' + values['headers']).show().empty();
					var pre = $("<pre/>");
					pre.text(response.getAllResponseHeaders());
					$('#' + values['headers']).append(pre);

					$('#' + values['result']).show().empty();
					if (response.getResponseHeader('Content-Type').startsWith(
							"text/html")) {
						$('#' + values['result']).html(
								$(str2DOMElement(data)).find("body div"));
					} else {
						var pre = $("<pre/>");
						pre.text(response.responseText);
						$('#' + values['result']).append(pre);
					}
				},
				error : function(response, status, textStatus) {
					var i = $('#' + values['info']).removeClass().addClass(
							'panel panel-danger');
					$(i).find('.panel-heading').empty().append(
							'<span>' + response.statusCode().status + ' '
									+ textStatus + '</span>');
					if (response.responseText) {
						$('#' + values['result']).html(response.responseText);
					}
				}
			});
		} else {
			$('#' + values['info']).removeClass()
					.addClass('panel panel-danger').find('.panel-heading')
					.empty().append('Wrong request. Missing parameter.');
		}

	});
});
