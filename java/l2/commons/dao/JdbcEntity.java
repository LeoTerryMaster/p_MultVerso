package l2.commons.dao;

import java.io.Serializable;

public interface JdbcEntity extends Serializable
{
	JdbcEntityState getJdbcState();
	
	void setJdbcState(JdbcEntityState state);
	
	void save();
	
	void update();
	
	void delete();
}