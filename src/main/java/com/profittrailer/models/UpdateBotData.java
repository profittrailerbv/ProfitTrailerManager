package com.profittrailer.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateBotData {

	private List<String> botsToUpdate = new ArrayList<>();
	private String currentlyUpdatingBot = "";
	private String updateUrl ="";

	public UpdateBotData(String updateUrl) {
		this.updateUrl = updateUrl;
	}
}
