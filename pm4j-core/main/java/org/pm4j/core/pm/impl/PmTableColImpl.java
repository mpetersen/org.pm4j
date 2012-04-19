package org.pm4j.core.pm.impl;

import java.util.Comparator;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.pageable.PageableCollection;
import org.pm4j.core.util.table.ColSizeSpec;

/**
 * Implements the table column PM behavior.
 *
 * @author olaf boede
 */
public class PmTableColImpl extends PmObjectBase implements PmTableCol {

  public final PmAttrEnum<PmSortOrder> defaultSortOrderAttr = new SortOrderAttr(this);
  public final PmCommand defaultCmdSort = new CmdSortPm();
  public final PmAttrInteger defaultColPosAttr = new PmAttrIntegerImpl(this);

  /**
   * A specific row sort comparator. The type of items to compare depends on the kind of {@link PageableCollection} container
   * behind the table.
   */
  private Comparator<?> rowSortComparator;

  /**
   * The filter that is active for this column.
   */
  private Filter<?> rowFilter;

  public PmTableColImpl(PmTable<?> pmTable) {
    this(pmTable, null);
  }

  /**
   * @param pmTable
   *          The table containing this column.
   * @param rowSortComparator
   *          A specific row sort comparator. The type of items to compare
   *          depends on the kind of {@link PageableCollection} container behind the
   *          table.
   */
  public PmTableColImpl(PmTable<?> pmTable, Comparator<?> rowSortComparator) {
    super(pmTable);
    this.rowSortComparator = rowSortComparator;
  }

  @Override
  protected void onPmInit() {
    super.onPmInit();

    PmEventApi.addPmEventListener(getSortOrderAttr(), PmEvent.VALUE_CHANGE, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        PmTableImpl<?> tablePm = (PmTableImpl<?>) getPmParent();
        tablePm.onSortOrderChange(PmTableColImpl.this);
      }
    });
  }


  @Override
  public ColSizeSpec getPmColSize() {
    return getOwnMetaData().colSizeSpec;
  }

  @Override
  public PmAttrEnum<PmSortOrder> getSortOrderAttr() {
    return defaultSortOrderAttr;
  }

  @Override
  public PmCommand getCmdSort() {
    return defaultCmdSort;
  }

  @Override
  public PmAttrInteger getColPosAttr() {
    return defaultColPosAttr;
  }

  /**
   * Creates the PM for the <code>sortOrderAttr</code> attribute.<br>
   * Concrete subclasses may define here their specific sortOrderAttr PM.
   *
   * @return The <code>sortOrderAttr</code> attribute to use.<br>
   *         Should be not visible if this column is not sortable.
   */
  protected PmAttrEnum<PmSortOrder> makeSortOrderAttr() {
    return new SortOrderAttr(this);
  }

  protected PmCommand makeCmdSort() {
    return new CmdSortPm();
  }

  @PmCommandCfg(beforeDo=BEFORE_DO.VALIDATE)
  protected class CmdSortPm extends PmCommandImpl {

    public CmdSortPm() {
      super(PmTableColImpl.this);
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return getSortOrderAttr().isPmEnabled();
    }

    @Override
    protected boolean isPmVisibleImpl() {
      return getSortOrderAttr().isPmVisible();
    }

    @Override
    public String getPmIconPath() {
      String path;
      PmAttrEnum<PmSortOrder> sortAttr = getSortOrderAttr();

      switch (sortAttr.getValue()) {
        case ASC: path = "pmSortOrder.ASC_icon"; break;
        case DESC: path = "pmSortOrder.DESC_icon"; break;
        case NEUTRAL: path = "pmSortOrder.NEUTRAL_icon"; break;
        default: throw new PmRuntimeException(this, "Unknown enum value: " + sortAttr.getValue());
      }

      return PmLocalizeApi.localize(this, isPmEnabled()
                                          ? path
                                          : path + "Disabled");
    }

    @Override
    protected void doItImpl() {
      PmAttrEnum<PmSortOrder> sortAttr = getSortOrderAttr();
      switch (sortAttr.getValue()) {
        case ASC: sortAttr.setValue(PmSortOrder.DESC); break;
        case DESC: sortAttr.setValue(PmSortOrder.NEUTRAL); break;
        case NEUTRAL: sortAttr.setValue(PmSortOrder.ASC); break;
        default: throw new PmRuntimeException(this, "Unknown enum value: " + sortAttr.getValue());
      }
    }

    @Override
    protected PmDataInput getValidationExecRootPm() {
      return PmUtil.getPmParentOfType(this, PmTable.class);
    }

    @Override
    protected PmObject getValidationErrorRootPm() {
      return PmUtil.getPmParentOfType(this, PmTable.class);
    }
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return The {@link PmTable} that contains this column.
   */
  private PmTable<?> getPmTable() {
    return (PmTable<?>)getPmParent();
  }

  @Override
  public Comparator<?> getRowSortComparator() {
    return rowSortComparator;
  }

  @Override
  public Filter<?> getRowFilter() {
    return rowFilter;
  }

  public void setRowFilter(Filter<?> rowFilter) {
    this.rowFilter = rowFilter;
  }


  // ======== Meta data ======== //

  @Override
  protected org.pm4j.core.pm.impl.PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    // myMetaData.setConverterDefault(PmConverterLong.INSTANCE);

    PmTableColCfg annotation = AnnotationUtil.findAnnotation(this, PmTableColCfg.class);
    if (annotation != null) {
      myMetaData.colSizeSpec = new ColSizeSpec(
          annotation.prefSize(), annotation.minSize(), annotation.maxSize());

      if (annotation.sortable() != PmBoolean.UNDEFINED) {
        myMetaData.sortable = annotation.sortable();
      }
      else {
      }
    }

    if (myMetaData.sortable == PmBoolean.UNDEFINED) {
      PmTableCfg tableCfg = AnnotationUtil.findAnnotation((PmObjectBase)getPmParent(), PmTableCfg.class);
      myMetaData.sortable = (tableCfg != null &&
                             tableCfg.sortable() == PmBoolean.TRUE)
                               ? PmBoolean.TRUE
                               : PmBoolean.FALSE;
    }
  }

  protected static class MetaData extends PmObjectBase.MetaData {
    private ColSizeSpec colSizeSpec = null;
    private PmBoolean sortable = PmBoolean.UNDEFINED;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }


  /**
   * Default sort order PM attribute class.
   * <p>
   * Changes the value only after successful validation if the table content.
   * <p>
   * May be extended or replaced by domain specific implementations.
   */
  public class SortOrderAttr extends PmAttrEnumImpl<PmSortOrder> {
    public SortOrderAttr(PmObject pmParent) {
      super(pmParent, PmSortOrder.class);
    }

    @Override
    protected PmSortOrder getDefaultValueImpl() {
      return PmSortOrder.NEUTRAL;
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return  (getOwnMetaData().sortable != PmBoolean.FALSE) &&
              (getPmTable().getTotalNumOfRows() > 1); // TODO olaf: The set of visible rows would be better...
    }

    @Override
    protected boolean isPmVisibleImpl() {
      return isPmEnabledImpl();
    }

    /**
     * The set value operation is overridden to ensure table validity before setting the value.
     * This was not done within {@link #setBackingValueImpl(PmSortOrder)} because this gets also called
     * by internal functionality. E.g. on {@link #getValue()} to apply the default value.
     */
    @Override
    protected boolean setValueImpl(SetValueContainer<PmSortOrder> value) {
      // XXX olaf: a set value decorator would be better.
      return PmValidationApi.validateSubTree(PmUtil.getPmParentOfType(this, PmTable.class))
          ? super.setValueImpl(value)
          : false;
    }
  }

}
