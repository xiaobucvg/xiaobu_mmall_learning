package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件服务
 * 2018/12/9 15:40
 * @author zh_job
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
	private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

	public String uploadFile(MultipartFile file,String path){
		if(file == null || file.getSize() == 0L){
			return null;
		}
		File filePath = new File(path);
		if(!filePath.exists()){
			boolean mk = filePath.mkdirs();
			boolean set = filePath.setWritable(true);// 赋予权限
			logger.info("创建有目录{},{},赋予可写入权限{}.",filePath,mk,set);
		}
		String oldFileName = file.getOriginalFilename(); // 获取原始文件名
		String extensionName = oldFileName.substring(oldFileName.indexOf(".")); //获取扩展名
		String newFileName = UUID.randomUUID().toString() + extensionName;
		logger.info("即将上传文件.原始文件名{},上传的路径{},新文件名{}",oldFileName,path,newFileName);
		File targetFile = new File(path,newFileName);
		try {
			file.transferTo(targetFile);
			//上传到FTP文件服务器 img目录下
			boolean status = FTPUtil.upload(Lists.newArrayList(targetFile),"img");
			//删除站点下面的文件
			if(status){
				boolean del = targetFile.delete();
				logger.info("删除文件{}.",del);
			}
			return targetFile.getName();
		} catch (IOException e) {
			logger.error("文件转换异常.",e);
		}
		return null;
	}
}
