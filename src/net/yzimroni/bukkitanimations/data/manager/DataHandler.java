package net.yzimroni.bukkitanimations.data.manager;

import net.yzimroni.bukkitanimations.data.action.ActionData;

public interface DataHandler<T> {

	void save(ActionData action, T object);

	void load(ActionData action, T object);

}
