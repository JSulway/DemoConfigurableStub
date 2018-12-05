package com.demo.stub.jaxb;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.demo.stub.handler.JAXBBase;

@XmlRootElement( name = "DemoStubRequest" )
public class DemoStubRequest extends JAXBBase {
	
		String planetId;
		String planetName;
		Date planetCreatedDate;
		Date updatedDatetime;
		String classType;
		String advancementLevel;
		String primarySpecies;
		String population;
		String language;
		String climate;
		String notes;
		
		public String getPlanetId() {
			return planetId;
		}
		@XmlElement
		public void setPlanetId(String planetId) {
			this.planetId = planetId;
		}
		public String getPlanetName() {
			return planetName;
		}
		@XmlElement
		public void setPlanetName(String planetName) {
			this.planetName = planetName;
		}
		public Date getPlanetCreatedDate() {
			return planetCreatedDate;
		}
		@XmlElement
		public void setPlanetCreatedDate(Date planetCreatedDate) {
			this.planetCreatedDate = planetCreatedDate;
		}
		public Date getUpdatedDatetime() {
			return updatedDatetime;
		}
		@XmlElement
		public void setUpdatedDatetime(Date updatedDatetime) {
			this.updatedDatetime = updatedDatetime;
		}
		public String getClassType() {
			return classType;
		}
		@XmlElement
		public void setClassType(String classType) {
			this.classType = classType;
		}
		public String getAdvancementLevel() {
			return advancementLevel;
		}
		@XmlElement
		public void setAdvancementLevel(String advancementLevel) {
			this.advancementLevel = advancementLevel;
		}
		public String getPrimarySpecies() {
			return primarySpecies;
		}
		@XmlElement
		public void setPrimarySpecies(String primarySpecies) {
			this.primarySpecies = primarySpecies;
		}
		public String getPopulation() {
			return population;
		}
		@XmlElement
		public void setPopulation(String population) {
			this.population = population;
		}
		public String getLanguage() {
			return language;
		}
		@XmlElement
		public void setLanguage(String language) {
			this.language = language;
		}
		public String getClimate() {
			return climate;
		}
		@XmlElement
		public void setClimate(String climate) {
			this.climate = climate;
		}
		public String getNotes() {
			return notes;
		}
		@XmlElement
		public void setNotes(String notes) {
			this.notes = notes;
		}
}
