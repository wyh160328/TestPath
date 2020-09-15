var EncryptExportRegexItem = function() {
	this.itemName = "${EncryptExportRegex_20190801}";
	this.dbKey = "ENCRYPT_EXPORT_REGEX";
	this.defaultValue = "";
};
lang.extend(EncryptExportRegexItem,
'freequery.config.configitem.AbstractSystemConfigItem');

//进行初始化动作，并返回一个tr元素
EncryptExportRegexItem.prototype.init = function() {
	debugger;
	this.tr = document.createElement("tr");
	this.tr.height = "30";

	this.td1 = document.createElement("td");
	this.td1.align = "left";
	this.td1.width = "200px";
	this.td1.innerHTML = this.itemName + "：";
	this.tr.appendChild(this.td1);

	this.td2 = document.createElement("td");
	this.td2.innerHTML = "<input type='text' name='encryptExportRegex' bofid='encryptExportRegex'"
			+ " style='width:100%;'/>";
	this.tr.appendChild(this.td2);
	
	//初始化标签
	this.td3 = document.createElement("td");
	var displayDefaultValue = this.defaultValue === '' ? '空白' : this.defaultValue;
	this.td3.innerHTML = "${InitialValueMessage_20190801}( " + displayDefaultValue + " )";
	this.tr.appendChild(this.td3);
	
	//初始化按钮
	this.td4 = document.createElement("td");
	this.td4.innerHTML = "<input class='button-buttonbar-noimage _defBtn ' value='${ResetInitialValueBtn_20190801}' "
			+ "type='button' style='width:100%;' />";
	this.tr.appendChild(this.td4);

	//获取input对象以及button对象
	this.encryptExportRegexInput = domutils.findElementByBofid([ this.tr ],
			"encryptExportRegex");

	this.resetBtn = domutils.findElementByClassName([ this.tr ], "_defBtn");
	var that = this;
	
	//添加点击后恢复初始值事件
	this.addListener(this.resetBtn, "click", function() {
		that.encryptExportRegexInput.value = this.defaultValue;
	}, this);
	
	return this.tr;
}

//检查配置信息是否合法	
EncryptExportRegexItem.prototype.validate = function() {
	return true;
}


//保存配置并返回是否保存成功	，对于从系统配置表里的获取数据的配置项来说，返回一个对象
EncryptExportRegexItem.prototype.save = function() {
	if (!this.validate())
		return false;
	var obj = {
		key : this.dbKey,
		value : '' + this.encryptExportRegexInput.value
	};
	debugger;
	return obj;
}


//对于从系统配置表里的获取数据的配置项来说，需要在初始化后根据知识库中的配置信息来显示
EncryptExportRegexItem.prototype.handleConfig = function(systemConfig) {
	for ( var i in systemConfig) {
		var config = systemConfig[i];
		if (config && config.key == this.dbKey) {
			var v = config.value;//对于非法请求的提示信息
			if (v) {
				this.encryptExportRegexInput.value = v;
				return;
			} else {
				this.encryptExportRegexInput.value = this.defaultValue;
				return;
			}				
		}
	}
	//数据库中尚未有此配置项时
	this.encryptExportRegexInput.value = this.defaultValue;
};