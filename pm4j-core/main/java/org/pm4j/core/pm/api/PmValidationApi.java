package org.pm4j.core.pm.api;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * API for PM validation related operations.
 * <p>
 * TODO olaf: needs to be completed.
 *
 * @author olaf boede
 */
public final class PmValidationApi {

  private static final Log LOG = LogFactory.getLog(PmValidationApi.class);

  /**
   * Validates the sub tree of the given PM.
   * <p>
   * In case of validation issues it generates validation messages for within the related {@link PmConversation}.
   *
   * @param validationExecTreeRootPm
   *            The root of the PM tree to validate.
   * @return The set of error messages that was generated by this call.
   */
  public static boolean validateSubTree(PmDataInput validationExecTreeRootPm) {
    return validateSubTree(validationExecTreeRootPm, validationExecTreeRootPm);
  }

  public static boolean validateSubTree(PmDataInput validationExecTreeRootPm, PmObject requiredValidSubtreeRoot) {
      PmConversationImpl pmConversation = (PmConversationImpl) validationExecTreeRootPm.getPmConversation();

      // Clear existing not attribute related messages within the validation exec scope.
      // Attribute messages can't be cleared because the conversion errors are handled internally by the attribute validation.
      // XXX olaf: This logic needs to be simplified!
      for (PmMessage m : PmMessageUtil.getSubTreeMessages(validationExecTreeRootPm, Severity.INFO)) {
        if (m.getSeverity().ordinal() < Severity.ERROR.ordinal() ||
            !(m.getPm() instanceof PmAttr)) {
          pmConversation.clearPmMessage(m);
        }
      }

      List<PmMessage> nonSubtreeMessagesBefore = PmMessageUtil.getSubTreeMessages(pmConversation, Severity.ERROR);

      validationExecTreeRootPm.pmValidate();


      List<PmMessage> conversationMessagesAfter = PmMessageUtil.getSubTreeMessages(pmConversation, Severity.ERROR);

      if (conversationMessagesAfter.isEmpty()) {
        return true;
      }

      List<PmMessage> ownMessages = PmMessageUtil.getSubTreeMessages(requiredValidSubtreeRoot, Severity.ERROR);

      // catch all other new error messages generated by this call. (caused by runtime exceptions etc.)
      conversationMessagesAfter.removeAll(ownMessages);
      conversationMessagesAfter.removeAll(nonSubtreeMessagesBefore);
      ownMessages.addAll(conversationMessagesAfter);

      if (! ownMessages.isEmpty()) {
        if (LOG.isDebugEnabled()) {
          StringBuilder sb = new StringBuilder(160);
          sb.append("Validation errors found:");
          for (PmMessage m : ownMessages) {
            sb.append("\n\t")
              .append(m.getPm().getPmRelativeName()).append(" - '")
              .append(m.getPm().getPmTitle()).append("': ")
              .append(m.getTitle());
          }
          LOG.debug(sb.toString());
        }
        return false;
      }
      else {
        return true;
      }
  }

  /**
   * Clears not yet validated values within the scope of this PM.
   */
  public static void clearInvalidValuesOfSubtree(PmObject pm) {
    for (PmObject p : pm.getPmConversation().getPmsWithInvalidValues()) {
      if (PmUtil.isChild(pm, p)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Cleaning invalid value state for attribute '" + PmUtil.getPmLogString(p) + "'.");
        }
        ((PmObjectBase)p).clearPmInvalidValues();
      }
    }
  }

  /**
   * @param pm The element to check.
   * @return <code>true</code> if all attributes do not have an error state.
   */
  public static boolean hasValidAttributes(PmElement pm) {
    for (PmAttr<?> a : PmUtil.getPmChildrenOfType(pm, PmAttr.class)) {
      if (!a.isPmValid()) {
        return false;
      }
    }
    // all attributes are valid
    return true;
  }


}
