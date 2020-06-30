/*
 * $Id: Manageable.java,v 1.37 2012/02/28 18:10:54 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.manageable;

import java.util.Collections;
import java.util.Map;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;

/**
 * Abstraction of a manageable object. Abstract base class for classes that
 * support bootstrap, initialization and deinitialization functions.
 *
 * <p>In environments where <code>Manageable</code> instances are constructed
 * dynamically, they are typically expected to have a public no-argument
 * constructor.
 *
 * <p>Initially the state of a manageable object is {@link #UNUSABLE}. In this
 * state, the object should be considered unusable. To change to the
 * {@link #USABLE} state, the {@link #bootstrap(Map)} and
 * {@link #init(Map)} methods should be called first, as described
 * below.
 *
 * <p>The {@link #bootstrap(Map)} method can only be called if the
 * state of this object is {@link #UNUSABLE}. If it finishes successfully, the
 * state then changes to {@link #BOOTSTRAPPED}.
 *
 * <p>After that the {@link #init(Map)} method should be called to
 * initialize or re-initialize this object. This method can only be called
 * successfully if the current state is either {@link #BOOTSTRAPPED} or even
 * {@link #USABLE}.
 *
 * <p>The {@link #deinit()} method is called when this object is no
 * longer needed. That changes the state back to {@link #UNUSABLE}. After
 * that, {@link #bootstrap(Map)} could be called again, though.
 *
 * @version $Revision: 1.37 $ $Date: 2012/02/28 18:10:54 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public abstract class Manageable {

   /**
    * The <em>UNUSABLE</em> state.
    */
   public static final State UNUSABLE = new State(0, "UNUSABLE");

   /**
    * The <em>BOOTSTRAPPING</em> state.
    */
   public static final State BOOTSTRAPPING = new State(2, "BOOTSTRAPPING");

   /**
    * The <em>BOOTSTRAPPED</em> state.
    */
   public static final State BOOTSTRAPPED = new State(3, "BOOTSTRAPPED");

   /**
    * The <em>INITIALIZING</em> state.
    */
   public static final State INITIALIZING = new State(4, "INITIALIZING");

   /**
    * The <em>USABLE</em> state.
    */
   public static final State USABLE = new State(5, "USABLE");

   /**
    * The <em>DEINITIALIZING</em> state.
    */
   public static final State DEINITIALIZING = new State(1, "DEINITIALIZING");

   /**
    * The state of this manageable object.
    */
   private State _state;

   /**
    * The lock for the state object.
    */
   private Object _stateLock;

   /**
    * Constructs a new <code>Manageable</code>.
    */
   protected Manageable() {
      _state     = UNUSABLE;
      _stateLock = new Object();
   }

   /**
    * Gets the current state of this object.
    *
    * @return
    *    the current state, never <code>null</code>.
    */
   public final State getState() {
      return _state;
   }

   /**
    * Performs the bootstrap procedure (wrapper method).
    *
    * <p>If the state of this object is valid (it must be {@link #UNUSABLE})
    * and the argument is not <code>null</code>, then
    * {@link #bootstrapImpl(Map)} will be called. If that method
    * succeeds, then this object will be left in the {@link #BOOTSTRAPPED}
    * state.
    *
    * <p>If {@link #bootstrapImpl(Map)} throws any exception (even
    * {@link Error}s), it is wrapped in an {@link BootstrapException} and then
    * the latter is thrown instead.
    *
    * @param properties
    *    the bootstrap properties, can be <code>null</code>.
    *
    * @throws IllegalStateException
    *    if the current state is not {@link #UNUSABLE}.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if the value of a certain property is invalid.
    *
    * @throws BootstrapException
    *    if the bootstrapping failed for any other reason.
    */
   public final void bootstrap(Map<String, String> properties)
   throws IllegalStateException,
          MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {

      State erroneousState = null;

      // Get the current state and change to BOOTSTRAPPING if it is valid
      synchronized (_stateLock) {
         if (_state != UNUSABLE) {
            erroneousState = _state;
         } else {
            _state = BOOTSTRAPPING;
         }
      }

      // If the state was invalid, then fail
      if (erroneousState != null) {
         final String MESSAGE = "The current state is "
                              + erroneousState
                              + " instead of UNUSABLE.";
         throw new IllegalStateException(MESSAGE);
      }

      // If no properties are passed, then use an empty set
      if (properties == null) {
         properties = Collections.EMPTY_MAP;
      }

      // Delegate to subclass
      State newState = UNUSABLE;
      try {
         bootstrapImpl(properties);
         newState = BOOTSTRAPPED;

      // Catch expected exceptions
      } catch (MissingRequiredPropertyException exception) {
         throw exception;
      } catch (InvalidPropertyValueException exception) {
         throw exception;
      } catch (BootstrapException exception) {
         throw exception;

      // Wrap other exceptions in an InitializationException
      } catch (Throwable exception) {
         throw new BootstrapException(exception);

      // Always set the state before returning
      } finally {
         synchronized (_stateLock) {
            _state = newState;
         }
      }
   }

   /**
    * Performs the bootstrap procedure (actual implementation). When this
    * method is called from {@link #bootstrap(Map)}, the state and
    * the argument will have been checked and the state will have been set to
    * {@link #BOOTSTRAPPING}.
    *
    * <p>The implementation of this method in class {@link Manageable} is
    * empty.
    *
    * @param properties
    *    the bootstrap properties, not <code>null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if the value of a certain property is invalid.
    *
    * @throws BootstrapException
    *    if the bootstrapping failed for any other reason.
    */
   protected void bootstrapImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {
      // empty
   }

   /**
    * Performs the initialization procedure (wrapper method).
    *
    * <p>If the state of this object is valid (it must be either
    * {@link #BOOTSTRAPPED} or {@link #USABLE}) and the argument is not
    * <code>null</code>, then {@link #initImpl(Map)} will be
    * called. If that method succeeds, then this object will be left in the
    * {@link #USABLE} state. If an exception is thrown, then this object will
    * be left in the {@link #BOOTSTRAPPED} state instead.
    *
    * <p>If {@link #initImpl(Map)} throws any exception (even
    * {@link Error}s), it is wrapped in an {@link InitializationException} and
    * then the latter is thrown instead.
    *
    * @param properties
    *    the initialization properties, can be <code>null</code>.
    *
    * @throws IllegalStateException
    *    if the current state is not {@link #BOOTSTRAPPED} or {@link #USABLE}.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if the value of a certain property is invalid.
    *
    * @throws InitializationException
    *    if the initialization failed for any other reason.
    */
   public final void init(Map<String, String> properties)
   throws IllegalStateException,
          MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {

      State erroneousState = null;

      // Get the current state and change to INITIALIZING if it is valid
      synchronized (_stateLock) {
         if (_state != BOOTSTRAPPED && _state != USABLE) {
            erroneousState = _state;
         } else {
            _state = INITIALIZING;
         }
      }

      // If the state was invalid, then fail
      if (erroneousState != null) {
         String message = "The current state is "
                        + erroneousState
                        + " instead of either "
                        + BOOTSTRAPPED
                        + " or "
                        + USABLE
                        + '.';
         throw new IllegalStateException(message);
      }

      // If no properties are passed, then use an empty set
      if (properties == null) {
         properties = Collections.EMPTY_MAP;
      }

      // Delegate to subclass
      State newState = BOOTSTRAPPED;
      try {
         initImpl(properties);
         newState = USABLE;

      // Catch expected exceptions
      } catch (MissingRequiredPropertyException exception) {
         throw exception;
      } catch (InvalidPropertyValueException exception) {
         throw exception;
      } catch (InitializationException exception) {
         throw exception;

      // Wrap other exceptions in an InitializationException
      } catch (Throwable exception) {
         throw new InitializationException(exception);

      // Always set the state before returning
      } finally {
         synchronized (_stateLock) {
            _state = newState;
         }
      }
   }

   /**
    * Performs the initialization procedure (actual implementation). When this
    * method is called from {@link #init(Map)}, the state and the
    * argument will have been checked and the state will have been set to
    * {@link #INITIALIZING}.
    *
    * <p>The implementation of this method in class {@link Manageable} is
    * empty.
    *
    * @param properties
    *    the initialization properties, not <code>null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if the value of a certain property is invalid.
    *
    * @throws InitializationException
    *    if the initialization failed, for any other reason.
    */
   protected void initImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {
      // empty
   }

   /**
    * Deinitializes this instance (wrapper method). This method relies on
    * {@link #deinitImpl()} to actually perform the deinitialization.
    *
    * <p>The current state of this object must be either {@link #BOOTSTRAPPED}
    * or {@link #USABLE}.
    *
    * <p>When this method returns, the state has been set to
    * {@link #UNUSABLE}, even if {@link #deinitImpl()} threw an exception.
    *
    * <p>If {@link #deinitImpl()} throws any exception, it is wrapped in a
    * {@link DeinitializationException} and
    * then the latter is thrown instead.
    *
    * @throws IllegalStateException
    *    if the state is not {@link #BOOTSTRAPPED} nor {@link #USABLE}.
    *
    * @throws DeinitializationException
    *    if the deinitialization caused an exception in
    *    {@link #deinitImpl()}.
    */
   public final void deinit()
   throws IllegalStateException, DeinitializationException {

      State erroneousState = null;

      // Get the current state and change to DEINITIALIZING if it is valid
      synchronized (_stateLock) {
         if (_state != BOOTSTRAPPED && _state != USABLE) {
            erroneousState = _state;
         } else {
            _state = DEINITIALIZING;
         }
      }

      // If the state was invalid, then fail
      if (erroneousState != null) {
         String message = "The current state is "
                        + erroneousState
                        + " instead of either "
                        + BOOTSTRAPPED
                        + " or "
                        + USABLE
                        + '.';
         throw new IllegalStateException(message);
      }

      // Delegate to subclass
      State newState = BOOTSTRAPPED;
      try {
         deinitImpl();
         newState = UNUSABLE;

      // Catch and wrap all caught exceptions
      } catch (Throwable exception) {
         throw new DeinitializationException(exception);

      // Always set the state before returning
      } finally {
         synchronized (_stateLock) {
            _state = newState;
         }
      }
   }

   /**
    * Deinitializes this instance (actual implementation). This method will be
    * called from {@link #deinit()} each time the latter is called and it
    * finds that the state is correct. The state will have been set to
    * {@link #DEINITIALIZING}.
    *
    * @throws Throwable
    *    if the deinitialization caused an exception.
    */
   protected void deinitImpl()
   throws Throwable {
      // empty
   }

   /**
    * Determines if this object is currently bootstrapped. Even if this object
    * is already initialized, then it is still considered bootstrapped.
    *
    * @return
    *    <code>true</code> if this object is bootstrapped,
    *    <code>false</code> if it is not.
    *
    * @since XINS 1.5.0
    */
   public final boolean isBootstrapped() {
      State state;
      synchronized (_stateLock) {
         state = _state;
      }
      return state.getLevel() >= BOOTSTRAPPED.getLevel();
   }

   /**
    * Determines if this object is currently usable.
    *
    * @return
    *    <code>true</code> if this object is usable,
    *    <code>false</code> if it is not.
    */
   public final boolean isUsable() {
      State state;
      synchronized (_stateLock) {
         state = _state;
      }
      return state == USABLE;
   }

   /**
    * Asserts that this object is currently usable. If it is not, then an
    * {@link IllegalStateException} is thrown.
    *
    * @throws IllegalStateException
    *    if this object is not in the {@link #USABLE} state.
    */
   protected final void assertUsable()
   throws IllegalStateException {

      // Minimize the time the lock is held
      State state;
      synchronized (_stateLock) {
         state = _state;
      }

      // Construct and throw an exception, if appropriate
      if (state != USABLE) {
         String message = "The current state is "
                        + state
                        + " instead of "
                        + USABLE
                        + '.';
         throw new IllegalStateException(message);
      }
   }

   /**
    * State of a <code>Manageable</code> object.
    *
    * @version $Revision: 1.37 $ $Date: 2012/02/28 18:10:54 $
    * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
    *
    * @since XINS 1.0.0
    */
   public static final class State {
      /**
       * Constructs a new <code>State</code> object.
       *
       * @param level
       *    the level of this state.
       *
       * @param name
       *    the name of this state, cannot be <code>null</code>.
       *
       * @throws IllegalArgumentException
       *    if <code>name == null</code>.
       */
      private State(int level, String name) throws IllegalArgumentException {

         // Check preconditions
         MandatoryArgumentChecker.check("name", name);

         _level = level;
         _name  = name;
      }

      /**
       * The level of this state.
       */
      private final int _level;

      /**
       * The name of this state. Cannot be <code>null</code>.
       */
      private final String _name;

      /**
       * Returns the level of this state.
       *
       * @return
       *    the level of this state, cannot be <code>null</code>.
       */
      int getLevel() {
         return _level;
      }

      /**
       * Returns the name of this state.
       *
       * @return
       *    the name of this state, cannot be <code>null</code>.
       */
      public String getName() {
         return _name;
      }

      /**
       * Returns a textual representation of this object.
       *
       * @return
       *    the name of this state, never <code>null</code>.
       */
      public String toString() {
         return _name;
      }
   }
}
