package edu.gatech.chai.VRDR.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
public class LazyInitializer
{
        //
        // Summary:
        //     Initializes a target reference type with the type's default constructor if it
        //     hasn't already been initialized.
        //
        // Parameters:
        //   target:
        //     A reference of type T to initialize if it has not already been initialized.
        //
        // Type parameters:
        //   T:
        //     The type of the reference to be initialized.
        //
        // Returns:
        //     The initialized reference of type T.
        //
        // Exceptions:
        //   T:System.MemberAccessException:
        //     Permissions to access the constructor of type T were missing.
        //
        //   T:System.MissingMemberException:
        //     Type T does not have a default constructor.
        ///public static T EnsureInitialized<T>(ref T target) where T : class;
        public static <T> T ensureInitialized(T target, Object lockObject) {
            if (target == null) {
                synchronized (lockObject) {
                    if (target == null) {
                        // Initialize the target object
                    }
                }
            }
            return target;
        }

        //
        // Summary:
        //     Initializes a target reference or value type with its default constructor if
        //     it hasn't already been initialized.
        //
        // Parameters:
        //   target:
        //     A reference or value of type T to initialize if it hasn't already been initialized.
        //
        //   initialized:
        //     A reference to a Boolean value that determines whether the target has already
        //     been initialized.
        //
        //   syncLock:
        //     A reference to an Object used as the mutually exclusive lock for initializing
        //     target. If syncLock is null, a new Object will be instantiated.
        //
        // Type parameters:
        //   T:
        //     The type of the reference to be initialized.
        //
        // Returns:
        //     The initialized value of type T.
        //
        // Exceptions:
        //   T:System.MemberAccessException:
        //     Permissions to access the constructor of type T were missing.
        //
        //   T:System.MissingMemberException:
        //     Type T does not have a default constructor.
        ///public static T EnsureInitialized<T>(ref T target, ref boolean initialized, ref Object syncLock);
        public static <T> T ensureInitialized(T target, AtomicBoolean initialized, Object syncLock) {
            if (!initialized.get()) {
                synchronized (syncLock) {
                    if (!initialized.get()) {
                        // initialize target here
                        initialized.set(true);
                    }
                }
            }
            return target;
        }

        //
        // Summary:
        //     Initializes a target reference or value type by using a specified function if
        //     it hasn't already been initialized.
        //
        // Parameters:
        //   target:
        //     A reference or value of type T to initialize if it hasn't already been initialized.
        //
        //   initialized:
        //     A reference to a Boolean value that determines whether the target has already
        //     been initialized.
        //
        //   syncLock:
        //     A reference to an Object used as the mutually exclusive lock for initializing
        //     target. If syncLock is null, a new Object will be instantiated.
        //
        //   valueFactory:
        //     The function that is called to initialize the reference or value.
        //
        // Type parameters:
        //   T:
        //     The type of the reference to be initialized.
        //
        // Returns:
        //     The initialized value of type T.
        //
        // Exceptions:
        //   T:System.MemberAccessException:
        //     Permissions to access the constructor of type T were missing.
        //
        //   T:System.MissingMemberException:
        //     Type T does not have a default constructor.
        ///public static T EnsureInitialized<T>(ref T target, ref boolean initialized, ref Object syncLock, Func<T> valueFactory);
        public static <T> T ensureInitialized(T target, boolean initialized, Object syncLock, Supplier<T> valueFactory) {
            if (!initialized) {
                synchronized (syncLock) {
                    if (!initialized) {
                        target = valueFactory.get();
                        initialized = true;
                    }
                }
            }
            return target;
        }
        //
        // Summary:
        //     Initializes a target reference type by using a specified function if it hasn't
        //     already been initialized.
        //
        // Parameters:
        //   target:
        //     The reference of type T to initialize if it hasn't already been initialized.
        //
        //   valueFactory:
        //     The function that is called to initialize the reference.
        //
        // Type parameters:
        //   T:
        //     The reference type of the reference to be initialized.
        //
        // Returns:
        //     The initialized value of type T.
        //
        // Exceptions:
        //   T:System.MissingMemberException:
        //     Type T does not have a default constructor.
        //
        //   T:System.InvalidOperationException:
        //     valueFactory returned null (Nothing in Visual Basic).
        ///public static T EnsureInitialized<T>(ref T target, Func<T> valueFactory) where T : class;
        public static <T> T EnsureInitialized(AtomicReference<T> target, java.util.function.Supplier<T> valueFactory) {
            T current = target.get();
            if (current != null) {
                return current;
            }

            T newValue = valueFactory.get();
            if (target.compareAndSet(null, newValue)) {
                return newValue;
            } else {
                return target.get();
            }
        }
}
