package edu.sjsu.cmpe.cache.lab4client;

public interface CacheServiceInterface {
	public String get(long key);

    public void put(long key, String value);

    //Added wrt lab4 requirements
    public void delete(long key);
}
