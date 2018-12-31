package com.mmall.service;

import com.mmall.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhang
 */
public interface IFileService {
	String uploadFile(MultipartFile file, String path);
}
