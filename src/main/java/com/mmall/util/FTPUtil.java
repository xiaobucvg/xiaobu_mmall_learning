package com.mmall.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * FTP服务工具
 *
 * @author zh_job
 * 2018/12/9 15:48
 */
public class FTPUtil {
	private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

	private static String ftpServerAddress = PropertiesUtil.getValue("ftp.server.ip", "192.168.137.24");
	private static String ftpUser = PropertiesUtil.getValue("ftp.user", "ftpuser");
	private static String ftpPassword = PropertiesUtil.getValue("ftp.pass", "19971123");

	private FTPUtil(String ip, int port, String user, String password) {
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	public static boolean upload(List<File> files,String path) throws IOException {
		FTPUtil ftpUtil = new FTPUtil(ftpServerAddress, 21, ftpUser, ftpPassword);
		logger.info("开始连接FTP服务器.");
		boolean result = ftpUtil.upload(path, files);
		logger.info("结束上传，上传结果{}.",result);
		return result;
	}

	private boolean upload(String remoteAddress, List<File> files) throws IOException {
		InputStream inputStream = null;
		//连接FTP服务器
		if (!connectFtpServer(this.ip, this.port, this.user, this.password)) {
			return false;
		}
		try {
			ftpClient.makeDirectory(remoteAddress);
			ftpClient.changeWorkingDirectory(remoteAddress);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.setBufferSize(1024);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			for (File fileItem : files) {
				inputStream = new FileInputStream(fileItem);
				ftpClient.storeFile(fileItem.getName(), inputStream);
			}
			return true;
		} catch (IOException e) {
			logger.error("上传文件失败.", e);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			ftpClient.disconnect();
		}
		return false;
	}

	private boolean connectFtpServer(String ip, int port, String user, String password) {
		boolean status = false;
		ftpClient = new FTPClient();
		try {
			ftpClient.connect(ip, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			status = true;
		} catch (IOException e) {
			logger.error("连接FTP服务器失败.", e);
		}
		return status;
	}

	private String ip;
	private int port;
	private String user;
	private String password;
	private FTPClient ftpClient;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
