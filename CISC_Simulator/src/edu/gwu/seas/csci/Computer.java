package edu.gwu.seas.csci;

public class Computer {

	public static void main(String[] args) {
		//initialization of registers and memory
		Registers registers = new Registers();
		Memory mm = new Memory();//remember the first 6 addresses are preserved
		Instructions instruction = new Instructions();
	}

}
