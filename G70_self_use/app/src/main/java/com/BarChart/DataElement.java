package com.BarChart;

public class DataElement {
	private String name;
	private int color;
	private int value;

	public DataElement(String name, int value, int color) {
		this.name = name;
		this.value = value;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return this.color;
	}
}