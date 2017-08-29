package net.yzimroni.bukkitanimations.data.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.yzimroni.bukkitanimations.data.action.ActionData;

public class DataManager {

	private HashMap<Class<?>, DataHandler<?>> handlers = new HashMap<>();
	private DataHandler<?> globalDataHandler;

	public <T> void register(Class<T> type, DataHandler<T> handler) {
		handlers.put(type, handler);
	}

	@SuppressWarnings("unchecked")
	public <T> void save(ActionData action, Object object) {
		getHandlers(object).forEach(h -> h.save(action, object));
		if (globalDataHandler != null) {
			((DataHandler<T>) globalDataHandler).save(action, (T) object);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void save(ActionData action, Object object, Class<?>... classes) {
		for (Class<?> c : classes) {
			if (handlers.containsKey(c)) {
				DataHandler<T> t = (DataHandler<T>) handlers.get(c);
				t.save(action, (T) object);
			}
		}
		if (globalDataHandler != null) {
			((DataHandler<T>) globalDataHandler).save(action, (T) object);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void load(ActionData action, T object) {
		getHandlers(object).forEach(h -> h.load(action, object));
		if (globalDataHandler != null) {
			((DataHandler<T>) globalDataHandler).load(action, (T) object);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<DataHandler<T>> getHandlers(T object) {
		List<DataHandler<T>> list = new ArrayList<>();
		Class<?> superClass = object.getClass();
		while (superClass != null && superClass != Object.class) {
			if (handlers.containsKey(superClass)) {
				list.add((DataHandler<T>) handlers.get(superClass));
			}
			addOnlyOnce(list, scanInterfaces(superClass));
			superClass = superClass.getSuperclass();
		}
		Collections.reverse(list);
		return list;
	}

	@SuppressWarnings("unchecked")
	private <T> List<DataHandler<T>> scanInterfaces(Class<?> toCheck) {
		List<DataHandler<T>> list = new ArrayList<>();
		Class<?>[] interfaces = toCheck.getInterfaces();
		for (Class<?> i : interfaces) {
			if (handlers.containsKey(i)) {
				list.add((DataHandler<T>) handlers.get(i));
			}
			list.addAll(scanInterfaces(i));
		}
		return list;

	}

	private <T> void addOnlyOnce(List<T> to, List<T> from) {
		for (T t : from) {
			if (!to.contains(t)) {
				to.add(t);
			}
		}
	}

	public DataHandler<?> getGlobalDataHandler() {
		return globalDataHandler;
	}

	public void setGlobalDataHandler(DataHandler<?> globalDataHandler) {
		this.globalDataHandler = globalDataHandler;
	}

}
