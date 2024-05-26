#!/usr/bin/env python
# coding: utf-8

import cv2
import numpy as np

FEATURES = 5000
MATCH_PERCENT = 0.8  # ปรับเป็นค่าที่น้อยลง

def align_image(aligned, ref):
    img1 = cv2.cvtColor(aligned, cv2.COLOR_BGR2GRAY)
    img2 = cv2.cvtColor(ref, cv2.COLOR_BGR2GRAY)
    
    # ใช้ SIFT แทน ORB
    sift_detector = cv2.SIFT_create(FEATURES)
    
    # คำนวณ keypoints และ descriptors
    keypoint1, descriptor1 = sift_detector.detectAndCompute(img1, None)
    keypoint2, descriptor2 = sift_detector.detectAndCompute(img2, None)
    
    # ใช้ BFMatcher และ NORM_L2 แทน crossCheck
    matcher = cv2.BFMatcher(cv2.NORM_L2)
    matches = matcher.knnMatch(descriptor1, descriptor2, k=2)
    
    # เลือก matches ที่ดีที่สุด
    good_matches = []
    for m, n in matches:
        if m.distance < MATCH_PERCENT * n.distance:
            good_matches.append(m)
    
    point1 = np.float32([keypoint1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
    point2 = np.float32([keypoint2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)
    
    # คำนวณ homography ด้วย RANSAC
    homography, mask = cv2.findHomography(point1, point2, cv2.RANSAC)
    
    height, width = img2.shape
    
    # ปรับขนาดภาพผลลัพธ์ให้เหมาะสม
    aligned_image = cv2.warpPerspective(aligned, homography, (width, height))
    
    return aligned_image