package com.example.demo.pojo;

public class PodMessage {

	
	

		private String containerGroup_Name;
		private String node_Name;
		private String status;
       private Double restarted;
		private Double created_time;
        private Double cPU;
		private Double memory;
		public String getContainerGroup_Name() {
			return containerGroup_Name;
		}
		public void setContainerGroup_Name(String containerGroup_Name) {
			this.containerGroup_Name = containerGroup_Name;
		}
		public String getNode_Name() {
			return node_Name;
		}
		public void setNode_Name(String node_Name) {
			this.node_Name = node_Name;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public Double getRestarted() {
			return restarted;
		}
		public void setRestarted(Double restarted) {
			this.restarted = restarted;
		}
		public Double getCreated_time() {
			return created_time;
		}
		public void setCreated_time(Double created_time) {
			this.created_time = created_time;
		}
		
		public Double getcPU() {
			return cPU;
		}
		public void setcPU(Double cPU) {
			this.cPU = cPU;
		}
		public Double getMemory() {
			return memory;
		}
		public void setMemory(Double memory) {
			this.memory = memory;
		}
		
	
}
