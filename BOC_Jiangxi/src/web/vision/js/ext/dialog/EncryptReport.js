/**
 * 报表加密导出密码输入弹框
 */
var BaseDialogEx = jsloader.resolve("freequery.dialog.BaseDialogEx");
var domutils = jsloader.resolve("freequery.lang.domutils");

var util = jsloader.resolve("freequery.common.util");
var encryptReport;
var EncryptReport = function() {
	encryptReport = this;
	EncryptReport.superclass.constructor.call(this);
};
lang.extend(EncryptReport, BaseDialogEx);

EncryptReport.prototype.init = function(parent, data, fn, obj) {
	EncryptReport.superclass.init.call(this, parent, data, fn, obj);
	var template = domutils.doGet("js/ext/dialog/EncryptReport.html");
	this.dialogBody.innerHTML = template;
}

// 提交表单 打包下载
EncryptReport.prototype.doOK = function() {
	var password = document.getElementById("passwordBtn");
	if (password.value == "") {
		alert("${inputpassword}");
		return;
	}
	//密码不符合正则表达式要求，直接返回
	if (!this.verifyRegex(password)) {
		var regexDescriptionStr = util.remoteInvoke('JXModule', 'getSystemConfigValue', ['REGEX_DESCRIPTION']);
		alert(regexDescriptionStr.result);
		return;
	}
	var form = document.getElementById("paramForm");
	var resid = document.getElementById("resid");
	var param = document.getElementById("param");
	var reportName = document.getElementById("reportName");
	var exportType = document.getElementById("exportType");
	var reportType = document.getElementById("reportType");
	var rowCount = document.getElementById("rowCount");
	// 处理多种不同报表的 resid
	resid.value = this.data.report.queryId || this.data.report.combinedQueryId
			|| this.data.report.dashboardId;
	// 设置参数
	var paramArray = []
	var params = this.data.report.params;
	if (params) {
		for (var n = 0; n < params.length; n++) {
			var itemobj = params[n];
			var obj = {
				id : itemobj.id,
				value : itemobj.value,
				displayValue : itemobj.displayValue
			};
			paramArray.push(obj)
		}
	}
	param.value = lang.toJSONString(paramArray);
	exportType.value = this.data.exportType;
	reportName.value = this.data.report.alias;
	reportType.value = this.data.reportType;
	rowCount.value = this.data.rowCount;
	form.submit();
	this.close()
}

//根据系统选项判断设置的密码是否符合正则表达式要求
EncryptReport.prototype.verifyRegex = function(password) {
	var regexPatternStr = util.remoteInvoke('JXModule', 'getSystemConfigValue', ['ENCRYPT_EXPORT_REGEX']);
	if (regexPatternStr && regexPatternStr.result !== null) {
		var regExp = new RegExp(regexPatternStr.result);	
		if (!regExp.test(password.value)) {
			return false;
		}
		return true;
	} else {
		//没有获取到系统选项的值
		alert("没有获取到系统选项");
		return false;
	}
}
