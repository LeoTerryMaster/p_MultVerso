package l2.commons.dao;

import java.io.Serializable;

public interface JdbcDAO<K extends Serializable, E extends JdbcEntity>
{
	E load(K key);
	
	void save(E e);
	
	void update(E e);
	
	void saveOrUpdate(E e);
	
	void delete(E e);
	
	JdbcEntityStats getStats();
}