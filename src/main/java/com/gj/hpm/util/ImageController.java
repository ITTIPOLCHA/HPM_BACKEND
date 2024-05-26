package com.gj.hpm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {

    public String processImage(MultipartFile image) {
        try {
            File tempFile = File.createTempFile("temp", ".jpg");
            image.transferTo(tempFile);
            
            // สร้าง ProcessBuilder และกำหนดคำสั่งให้เป็น python3 digit_recognize.py <ชื่อไฟล์ภาพ>
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("python3", "digit_recognize.py", tempFile.getAbsolutePath());
        
            // เริ่มกระบวนการด้วย ProcessBuilder
            Process process = processBuilder.start();
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // ลบไฟล์ชั่วคราวหลังจากใช้งานเสร็จแล้ว
            tempFile.delete();
        
            // รอให้กระบวนการ Python script เสร็จสิ้น
            int exitCode = process.waitFor();
        
            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error processing image: " + e.getMessage();
        }
        
    }
}
