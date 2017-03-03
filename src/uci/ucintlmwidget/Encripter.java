package uci.ucintlmwidget;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class Encripter {
	static String encriptPass = "UCIntlm+WIDGET-YCpV5fGzmo0ruVRwVfAD}1vjAJF";

	public static String encrypt(String cadena) {
		StandardPBEStringEncryptor s = new StandardPBEStringEncryptor();
		s.setPassword(encriptPass);
		return s.encrypt(cadena);
	}

	public static String decrypt(String cadena) {
		StandardPBEStringEncryptor s = new StandardPBEStringEncryptor();
		s.setPassword(encriptPass);
		String devuelve = "";
		try {
			devuelve = s.decrypt(cadena);
		} catch (Exception e) {
		}
		return devuelve;
	}
}
