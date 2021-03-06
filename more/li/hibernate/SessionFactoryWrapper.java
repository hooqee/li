package li.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

public class SessionFactoryWrapper implements SessionFactory {
    private static final long serialVersionUID = 8697863729776416273L;

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Reference getReference() throws NamingException {
        return this.getSessionFactory().getReference();
    }

    public void close() throws HibernateException {
        this.getSessionFactory().close();
    }

    public void evict(Class arg0) throws HibernateException {
        this.getSessionFactory().evict(arg0);
    }

    public void evict(Class arg0, Serializable arg1) throws HibernateException {
        this.getSessionFactory().evict(arg0, arg1);
    }

    public void evictCollection(String arg0) throws HibernateException {
        this.getSessionFactory().evictCollection(arg0);
    }

    public void evictCollection(String arg0, Serializable arg1) throws HibernateException {
        this.getSessionFactory().evictCollection(arg0, arg1);
    }

    public void evictEntity(String arg0) throws HibernateException {
        this.getSessionFactory().evictEntity(arg0);
    }

    public void evictEntity(String arg0, Serializable arg1) throws HibernateException {
        this.getSessionFactory().evictEntity(arg0, arg1);
    }

    public void evictQueries() throws HibernateException {
        this.getSessionFactory().evictQueries();
    }

    public void evictQueries(String arg0) throws HibernateException {
        this.getSessionFactory().evictQueries(arg0);
    }

    public Map getAllClassMetadata() throws HibernateException {
        return this.getSessionFactory().getAllClassMetadata();
    }

    public Map getAllCollectionMetadata() throws HibernateException {
        return this.getSessionFactory().getAllCollectionMetadata();
    }

    public ClassMetadata getClassMetadata(Class arg0) throws HibernateException {
        return this.getSessionFactory().getClassMetadata(arg0);
    }

    public ClassMetadata getClassMetadata(String arg0) throws HibernateException {
        return this.getSessionFactory().getClassMetadata(arg0);
    }

    public CollectionMetadata getCollectionMetadata(String arg0) throws HibernateException {
        return this.getSessionFactory().getCollectionMetadata(arg0);
    }

    public Session getCurrentSession() throws HibernateException {
        return this.getSessionFactory().getCurrentSession();
    }

    public Set getDefinedFilterNames() {
        return this.getSessionFactory().getDefinedFilterNames();
    }

    public FilterDefinition getFilterDefinition(String arg0) throws HibernateException {
        return this.getSessionFactory().getFilterDefinition(arg0);
    }

    public Statistics getStatistics() {
        return this.getSessionFactory().getStatistics();
    }

    public boolean isClosed() {
        return this.getSessionFactory().isClosed();
    }

    public Session openSession() throws HibernateException {
        return this.getSessionFactory().openSession();
    }

    public Session openSession(Connection arg0) {
        return this.getSessionFactory().openSession(arg0);
    }

    public Session openSession(Interceptor arg0) throws HibernateException {
        return this.getSessionFactory().openSession(arg0);
    }

    public Session openSession(Connection arg0, Interceptor arg1) {
        return this.getSessionFactory().openSession(arg0, arg1);
    }

    public StatelessSession openStatelessSession() {
        return this.getSessionFactory().openStatelessSession();
    }

    public StatelessSession openStatelessSession(Connection arg0) {
        return this.getSessionFactory().openStatelessSession(arg0);
    }
}