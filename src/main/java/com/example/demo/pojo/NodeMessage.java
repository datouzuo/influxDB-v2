package com.example.demo.pojo;

public class NodeMessage {

	
		private String node_Name;
		private boolean ready;
		
        private Double cPU_request_rate;
	
        private Double cPU_count;
       private Double memory_used;
        
        private Double memory_num;
       private Double  created_time;
	public String getNode_Name() {
		return node_Name;
	}
	public void setNode_Name(String node_Name) {
		this.node_Name = node_Name;
	}
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	public Double getcPU_request_rate() {
		return cPU_request_rate;
	}
	public void setcPU_request_rate(Double cPU_request_rate) {
		this.cPU_request_rate = cPU_request_rate;
	}
	public Double getcPU_count() {
		return cPU_count;
	}
	public void setcPU_count(Double cPU_count) {
		this.cPU_count = cPU_count;
	}
	public Double getMemory_used() {
		return memory_used;
	}
	public void setMemory_used(Double memory_used) {
		this.memory_used = memory_used;
	}
	public Double getMemory_num() {
		return memory_num;
	}
	public void setMemory_num(Double memory_num) {
		this.memory_num = memory_num;
	}
	public Double getCreated_time() {
		return created_time;
	}
	public void setCreated_time(Double created_time) {
		this.created_time = created_time;
	}
       
       
	}
	
	

