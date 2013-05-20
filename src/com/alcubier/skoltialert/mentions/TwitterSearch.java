package com.alcubier.skoltialert.mentions;


import java.util.List;

public class TwitterSearch {

	private List<TwitterMention> results;
	private long max_id;
	private String next_page;

	public List<TwitterMention> getResults() {
		return results;
	}
	public void setResults(List<TwitterMention> results) {
		this.results = results;
	}

    public String getNext_page() {
        return next_page;
    }
    public void setNext_page(String nextPage) {
        this.next_page = nextPage;
    }

    public long getMax_id() {
        return max_id;
    }
    public void setMax_id(long maxId) {
        this.max_id = maxId;
    }
    
}
	
