package com.automation.desktop.testdata;

import java.util.Random;

import com.automation.desktop.base.BaseSetup;
import com.github.javafaker.Faker;

public class TestData extends BaseSetup {

	static Faker faker = null;
	static StringBuilder sb = null;
	static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	static Random RANDOM = new Random();
	
	
	public static String RandomNameGenerator(int length) {
		sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
		}
		String randomName = sb.toString();
		return randomName; 
	}
	
	public static String UniqueValue(int length) {
		String Name = RandomNameGenerator(length);
		return Name;
	}
}