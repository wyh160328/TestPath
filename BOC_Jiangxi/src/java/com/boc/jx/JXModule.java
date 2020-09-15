package com.boc.jx;

import org.apache.log4j.Logger;

import smartbi.config.SystemConfigService;
import smartbi.framework.IModule;

/**
 * 中国银行江西省分行
 * 
 * @author luolisheng
 *
 */
public class JXModule implements IModule {
	private static final Logger LOG = Logger.getLogger(JXModule.class);
	private static JXModule instance;
	private static final String ENCRYPT_EXPORT_REGEX = "ENCRYPT_EXPORT_REGEX";
	private static final String REGEX_DESCRIPTION = "REGEX_DESCRIPTION";
	private static final String DEFAULT_ENCRYPT_EXPORT_REGEX = "";
	private static final String DEFAULT_DESCRIPTION_REGEX = "密码不符合正则表达式要求";

	public static JXModule getInstance() {
		if (instance == null)
			instance = new JXModule();
		return instance;
	}
	
	/**
	 * 获取密码正则表达式系统选项以及描述信息
	 * @param key
	 * @return
	 */
	public String getSystemConfigValue(String key) {
		String value = SystemConfigService.getInstance().getValue(key);
		//系统选项还没有设置，需要将系统选项配置进数据库中
		if (value == null) {
			if (ENCRYPT_EXPORT_REGEX.equals(key)) {
				SystemConfigService.getInstance().createSystemConfig(key, DEFAULT_ENCRYPT_EXPORT_REGEX, "");				
				return DEFAULT_ENCRYPT_EXPORT_REGEX;
			} else if (REGEX_DESCRIPTION.equals(key)) {
				SystemConfigService.getInstance().createSystemConfig(key, DEFAULT_DESCRIPTION_REGEX, "");								
				return DEFAULT_DESCRIPTION_REGEX;
			} else {
				return null;
			}
		}
		return value;
	}

	@Override
	public void activate() {
		LOG.info("JXModule activating...");
	}
}
