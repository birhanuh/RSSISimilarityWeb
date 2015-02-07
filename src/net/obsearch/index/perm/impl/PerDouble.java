package net.obsearch.index.perm.impl;
   
   public class PerDouble implements Comparable<PerDouble>{
   		private double distance;
   		private short id;
   		public PerDouble(double distance, short id) {
   			super();
   			this.distance = distance;
   			this.id = id;
  		}
  		@Override
  		public int compareTo(PerDouble o) {
  			if(distance < o.distance){
  				return -1;
  			}else if(distance > o.distance){
  				return 1;
  			}else{
  				if(id < o.id){
  					return -1;
  				}else if(id > o.id){
  					return 1;
  				}else{
  					return 0;
  				}
  			}
  		}
  
  		public boolean equals(Object o){
  				PerDouble ot = (PerDouble)o;
  				return  id == ot.id;
  		}
  		
  		
  		public int hashCode(){
  				return id;
  		}
  		
  		public double getDistance() {
  			return distance;
  		}
  		public void setDistance(double distance) {
  			this.distance = distance;
  		}
  		public short getId() {
  			return id;
  		}
  		public void setId(short id) {
  			this.id = id;
  		}
  		
  		
  		
  	}
