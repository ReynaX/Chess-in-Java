package board;

import java.util.HashMap;
import java.util.Map;

public class GameStateCache{
    private final Map<Integer, Double> m_cache;

    public GameStateCache(){
        m_cache = new HashMap<>();
    }

    public void add(Integer hashCode, double score){
        m_cache.put(hashCode, score);
    }

    public int size(){
        return m_cache.size();
    }

    public void clear(){
        m_cache.clear();
    }

    public double getScore(Integer hashCode){
        return m_cache.get(hashCode);
    }

    public boolean containsPosition(Integer hashCode){
        return m_cache.containsKey(hashCode);
    }
}
