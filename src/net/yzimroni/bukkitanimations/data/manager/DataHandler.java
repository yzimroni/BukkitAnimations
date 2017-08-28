package net.yzimroni.bukkitanimations.data.manager;

import net.yzimroni.bukkitanimations.data.action.ActionData;

public interface DataHandler<T> {

	public void save(ActionData action, T object);

	public void load(ActionData action, T object);

}
