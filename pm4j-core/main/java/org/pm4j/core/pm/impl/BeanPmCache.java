package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmBean;

interface BeanPmCache {

  /**
   * Registers the bean-to-PM mapping(s).
   *
   * @param pmElement A new PM for a bean.
   */
  void add(PmBean<?> pmElement);

  /**
   * Looks for a PM that already represents the given bean.
   *
   * @param bean The bean to find the PM for.
   * @return The related PM.
   */
  <T extends PmBean<?>> T findByBean(Object bean);

  /**
   * Removes the related mapping(s).
   * @param pmBean The obsolete PM.
   */
  void removePm(PmBean<?> pmBean);

  /**
   * Removes the related mapping(s).
   * @param bean The obsolete PM.
   */
  void removeBean(Object bean);

  /**
   * Clears all items.
   */
  void clear();

}
