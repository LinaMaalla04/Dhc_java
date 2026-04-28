package tn.dhc.services;
import java.util.List;

public interface IService<T> {
    public void ajouter(T t);
    public void supprimer(T t);
    public void modifier(T t);
    List<T> getAll();
    T getOneById(int id);
}
