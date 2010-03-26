/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.exxcellent.enterprise.dao;

import de.exxcellent.enterprise.dao.exceptions.NonexistentEntityException;
import de.exxcellent.enterprise.model.Entry;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author ostauss
 */
public class EntryJpaController {

    public EntryJpaController() {
        emf = Persistence.createEntityManagerFactory("enterprise");
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Entry entry) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(entry);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Entry entry) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            entry = em.merge(entry);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = entry.getId();
                if (findEntry(id) == null) {
                    throw new NonexistentEntityException("The entry with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Entry entry;
            try {
                entry = em.getReference(Entry.class, id);
                entry.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The entry with id " + id + " no longer exists.", enfe);
            }
            em.remove(entry);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Entry> findEntryEntities() {
        return findEntryEntities(true, -1, -1);
    }

    public List<Entry> findEntryEntities(int maxResults, int firstResult) {
        return findEntryEntities(false, maxResults, firstResult);
    }

    private List<Entry> findEntryEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Entry as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Entry findEntry(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Entry.class, id);
        } finally {
            em.close();
        }
    }

    public int getEntryCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Entry as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
