package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;

import java.util.List;

public interface AcHostRepository {

    /**
     * Saves a AC Host to the repository
     * @param acHost
     * @throws Throwable
     */
    void save(AcHost acHost) throws Throwable;

    /**
     * Retrieves a list of all the AC Host entries stored in the repository
     * @return
     * @throws Throwable
     */
    List<? extends AcHost> all() throws Throwable;

    /**
     * Retrieves a AC Host entry from the repository
     * @param key The unique key of the AC Host to retrieve
     * @return
     * @throws Throwable
     */
    Option<AcHost> findByKey(String key) throws Throwable;

    /**
     * Retrieves a AC Host entry from the repository
     * @param baseUrl The url of the AC Host to retrieve
     * @return
     * @throws Throwable
     */
    Option<AcHost> findByUrl(String baseUrl) throws Throwable;

    /**
     * Deletes a AC Host from the repository
     * @param id
     * @throws Throwable
     */
    void delete(Long id) throws Throwable;

    /**
     * Factory method to create a new empty instance of ACHost. This instance will not be stored in the Repository.
     * @return
     */
    AcHost create();
}
