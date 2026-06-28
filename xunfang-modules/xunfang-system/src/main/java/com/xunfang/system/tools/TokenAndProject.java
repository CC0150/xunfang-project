package com.xunfang.system.tools;

public class TokenAndProject {
	String token;
	String projectId;
	
	TokenAndProject(String token, String projectId){
		this.token = token;
		this.projectId = projectId;
	}
	
	@Override
	public String toString() {
		return this.token + ":" + this.projectId;
	}
	
	public String getToken() {return token;}
	public String getProjectId() {return projectId;}
}
