package com.boc.jx.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import smartbi.SmartbiException;
import smartbi.config.SystemConfigService;
import smartbi.net.sf.json.JSONArray;
import smartbi.net.sf.json.JSONObject;
import smartbi.sdk.service.combinedquery.CombinedReport;
import smartbi.sdk.service.graphicreport.GraphicReport;
import smartbi.sdk.service.insight.InsightReport;
import smartbi.sdk.service.simplereport.Parameter;
import smartbi.sdk.service.simplereport.Report;
import smartbi.sdk.service.spreadsheetreport.SSReport;
import smartbi.usermanager.local.LocalClientConnector;
import smartbi.util.CommonErrorCode;
import smartbi.util.StringUtil;

/**
 * 报表导出加密压缩统一处理 Servlet
 * 
 * @author luolisheng
 *
 */
public class ReportEncryptExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(ReportEncryptExportServlet.class);

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/vnd.ms-excel;charset=UTF-8");
		String resid = (String) request.getParameter("resid");
		String password = (String) request.getParameter("passwordBtn");
		String param = (String) request.getParameter("param");
		String reportName = (String) request.getParameter("reportName");
		String exportType = (String) request.getParameter("exportType");
		String type = (String) request.getParameter("reportType");
		String rowCount = (String) request.getParameter("rowCount");
		LOG.info("【reportName】=" + reportName + " 【resid】 =" + resid + " 【param】=" + param + " 【exportType】= "
				+ exportType + "【type】=" + type + "【rowCount】=" + rowCount);

		String maxrow = null;
		if (exportType == "EXCEL2007" || exportType == "LIST_EXCEL") {
			maxrow = SystemConfigService.getInstance().getValue("EXCEL_EXPORT_MAX_COUNT");
		}
		if (StringUtil.isNullOrEmpty(rowCount) && StringUtil.isNullOrEmpty(maxrow)) {
			rowCount = String.valueOf(Integer.MAX_VALUE);
		}
		LocalClientConnector connector = new LocalClientConnector();
		connector.openWithCurrentUser();
		JSONArray paramArray = null;
		if (!StringUtil.isNullOrEmpty(param)) {
			paramArray = JSONArray.fromString(param);
		}
		// 创建临时文件
		File file = new File(reportName + getFileExt(exportType));
		// 导出报表
		try {
			LOG.info(" executing file = getReportFile().....");
			file = getReportFile(file, type, paramArray, exportType, resid, rowCount);
		} catch (IOException e) {
			LOG.error("getReportFile IOException...");
			LOG.error(e.getMessage(), e);
		}
		// 添加密码
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile("temp.zip");
			ZipParameters parameters = new ZipParameters();
			// 设置压缩方法
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(password);
			LOG.info("executing zipFile.addFile...");
			zipFile.addFile(file, parameters);
			file.delete();
		} catch (ZipException e) {
			LOG.error("add zip file Exception e...");
			LOG.error(e.getMessage(), e);
		}
		
		String fileName = "";
		try {
			fileName = URLEncoder.encode("加密文件.zip", "UTF-8");
			LOG.info("fileName = " + fileName);
		} catch (UnsupportedEncodingException e) {
			LOG.error("URLEncoder encode error...");
			LOG.error(e.getMessage(), e);
		}
		response.setHeader("content-disposition", "attachment;filename=" + fileName);
		response.setHeader("content-type", "application/x-download");
		byte[] buf = new byte[2048];
		OutputStream ot = null;
		File zip = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			ot = response.getOutputStream();
			zip = zipFile.getFile();
			bis = new BufferedInputStream(new FileInputStream(zip));
			bos = new BufferedOutputStream(ot);
			int length = 0;
			LOG.info("executing outputstream.write...");
			while ((length = bis.read(buf)) > 0) {
				bos.write(buf, 0, length);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
					bos = null;
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (bis != null) {
				try {
					bis.close();
					bis = null;
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			zip.delete();
		}
	}

	/**
	 * 获取后缀名
	 * 
	 * @param type
	 * @return
	 */
	private String getFileExt(String type) {
		String ext = "";
		if (type.equals("HTML")) {
			ext = ".html";
		} else if (type.equals("EXCEL")) {
			ext = ".xls";
		} else if (type.equals("EXCEL2007") || type.equals("LIST_EXCEL")) {
			ext = ".xlsx";
		} else if (type.equals("WORD") || type.equals("WORD_ACTIVESHEET")) {
			ext = ".doc";
		} else if (type.equals("PDF") || type.equals("PRINT")) {
			ext = ".pdf";
		} else if (type.equals("PNG")) {
			ext = ".png";
		} else if (type.equals("SVG")) {
			ext = ".svg";
		} else if (type.equals("SWF")) {
			ext = ".swf";
		} else if (type.equals("ODS")) {
			ext = ".ods";
		} else if (type.equals("ODT")) {
			ext = ".odt";
		} else if (type.equals("CSV")) {
			ext = ".csv";
		} else if (type.equals("TXT")) {
			ext = ".txt";
		} else if (type.equals("MHT")) {
			ext = ".mht";
		} else if (type.equals("DATAPACKAGE")) {
			ext = ".sdatapkg";
		}
		return ext;
	}

	/**
	 * 
	 * @param file
	 *            文件
	 * @param type
	 *            报表类型
	 * @param jsonarray
	 *            参数数组
	 * @param exportType
	 *            导出类型
	 * @param reportID
	 *            报表id
	 * @param rowCount
	 *            导出行数
	 * @return
	 * @throws IOException
	 */
	private File getReportFile(File file, String type, JSONArray jsonarray, String exportType, String reportID,
			String rowCount) throws IOException {
		LocalClientConnector conn = new LocalClientConnector();
		conn.openWithCurrentUser();
		String spliticon = ",";
		String valueType = "value";
		rowCount += "&result=part";
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		switch (type) {
		case "SIMPLE_REPORT": {
			Report sdkReport = new Report(conn);
			sdkReport.open(reportID);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject obj = (JSONObject) jsonarray.get(i);
				sdkReport.setParamValue(obj.getString("id"), obj.getString("value"), obj.getString("displayValue"));
			}
			sdkReport.doExport(exportType, spliticon, rowCount, os);
			sdkReport.close();
			break;
		}
		case "Dashboard":
		case "DashboardMap": {
			GraphicReport sdkReport = new GraphicReport(conn);
			sdkReport.open(reportID);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject obj = (JSONObject) jsonarray.get(i);
				sdkReport.setParamValue(obj.getString("id"), obj.getString("value"), obj.getString("displayValue"));
			}
			sdkReport.doExport(exportType, os);
			sdkReport.close(sdkReport.getClientId());
			break;
		}
		case "SPREADSHEET_REPORT": {
			SSReport sdkReport = new SSReport(conn);
			sdkReport.open(reportID);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject obj = (JSONObject) jsonarray.get(i);
				sdkReport.setParamValue(obj.getString("id"), obj.getString("value"), obj.getString("displayValue"));
			}
			sdkReport.doExport(exportType, os);
			sdkReport.close();
			break;
		}
		case "COMBINED_QUERY": {
			CombinedReport sdkReport = new CombinedReport(conn);
			sdkReport.open(reportID);

			for (int i = 0; i < jsonarray.length(); i++) {
				String paramid = null;
				JSONObject obj = (JSONObject) jsonarray.get(i);
				List<Parameter> parameters = sdkReport.getParameters();
				// 由于组合分析后台每次都会生成一个可视化查询，导致参数id次次都会变化
				// 在这步做特殊处理。
				String preid = obj.getString("id").toString();
				preid = preid.substring(preid.lastIndexOf("."));
				for (Parameter parameter : parameters) {
					String nowid = parameter.getId();
					String nowidSub = nowid.substring(nowid.lastIndexOf("."));
					if (nowidSub.equals(preid)) {
						paramid = nowid;
						break;
					}
				}
				if (paramid != null) {
					sdkReport.setParamValue(paramid, obj.getString("value"), obj.getString("displayValue"));
				}
			}
			sdkReport.doExport(exportType, spliticon, rowCount, os);
			sdkReport.close();
			break;
		}
		case "INSIGHT": {
			InsightReport sdkReport = new InsightReport(conn);
			sdkReport.open(reportID);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject obj = (JSONObject) jsonarray.get(i);
				sdkReport.setParamValue(obj.getString("id"), obj.getString("value"), obj.getString("displayValue"));
			}
			sdkReport.doExport(exportType, spliticon, rowCount, os, "", valueType, "");
			sdkReport.close();
			break;
		}
		default:
			os.close();
			throw new SmartbiException(CommonErrorCode.UNKOWN_ERROR).setDetail("不支持该报表");
		}
		os.flush();
		os.close();
		return file;
	}
}
