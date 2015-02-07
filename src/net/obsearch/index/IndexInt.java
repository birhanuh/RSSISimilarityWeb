package net.obsearch.index;

import net.obsearch.Index;
import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.NotFrozenException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OutOfRangeException;
import net.obsearch.filter.Filter;
import net.obsearch.ob.OBInt;
import net.obsearch.result.OBPriorityQueueInt;

public abstract interface IndexInt<O extends OBInt> extends Index<O>
{
  public abstract void searchOB(O paramO, int paramInt, OBPriorityQueueInt<O> paramOBPriorityQueueInt)
    throws NotFrozenException, InstantiationException, IllegalIdException, IllegalAccessException, OutOfRangeException, OBException;

  public abstract void searchOB(O paramO, int paramInt, Filter<O> paramFilter, OBPriorityQueueInt<O> paramOBPriorityQueueInt)
    throws NotFrozenException, InstantiationException, IllegalIdException, IllegalAccessException, OutOfRangeException, OBException;

  public abstract int[] fullMatchLite(O paramO, boolean paramBoolean)
    throws OBException, IllegalAccessException, InstantiationException;
}

/* Location:           C:\Software\obsearch\obsearch-0.9.9g-jar-with-dependencies.jar
 * Qualified Name:     net.obsearch.index.IndexInt
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.5.3
 */