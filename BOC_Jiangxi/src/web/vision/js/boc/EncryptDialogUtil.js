/**
 * 加密压缩导出父类
 */
var dialogFactory = jsloader.resolve("freequery.dialog.dialogFactory");
var EncryptDialogUtil = function() {
	
};
/**
 * 加密导出弹框
 */
EncryptDialogUtil.prototype.exportEncrypt = function(that, typeId, reportType, rowCount) {
	var isLowVersionChrome = false;
	if (domutils.isChrome() && domutils.getBrowserVersion() < 40 && !(typeId == "CSV" || typeId == "LIST_EXCEL")) {
		isLowVersionChrome = true;
	}
	var data = {
			report : that,
			isLowVersionChrome : isLowVersionChrome,
			exportType : typeId,
			reportType : reportType,
			rowCount : rowCount
		}
		var dialogConfig = {
			size : [ 450, 200 ],
			title : '${input_title}',
			fullName : 'ext.dialog.EncryptReport'
		};
		dialogFactory.showDialog(dialogConfig, data);
}