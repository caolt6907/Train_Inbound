package com.strategy.vo;

public class SubjectBack implements Comparable<SubjectBack> {
	private String ResultWay;
	private String Intention;
	private int Priority;
	public String getResultWay() {
		return ResultWay;
	}
	public void setResultWay(String resultWay) {
		ResultWay = resultWay;
	}
	public String getIntention() {
		return Intention;
	}
	public void setIntention(String intention) {
		Intention = intention;
	}
	public int getPriority() {
		return Priority;
	}
	public void setPriority(int priority) {
		Priority = priority;
	}
	@Override
	public int compareTo(SubjectBack o) {
	
		 return new Integer(this.Priority).compareTo(o.getPriority()); 
	}
	
	

}
