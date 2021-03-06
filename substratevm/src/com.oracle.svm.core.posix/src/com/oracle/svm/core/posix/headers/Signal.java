/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.posix.headers;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.constant.CConstant;
import org.graalvm.nativeimage.c.constant.CEnum;
import org.graalvm.nativeimage.c.constant.CEnumValue;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CFieldAddress;
import org.graalvm.nativeimage.c.struct.CFieldOffset;
import org.graalvm.nativeimage.c.struct.CPointerTo;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.WordPointer;
import org.graalvm.nativeimage.impl.DeprecatedPlatform;
import org.graalvm.nativeimage.impl.InternalPlatform;
import org.graalvm.word.PointerBase;

/**
 * Contains definitions from signal.h that we actually need.
 */
@CContext(PosixDirectives.class)
public class Signal {
    /* Allow lower-case type names, underscores in names, etc.: Checkstyle: stop. */

    @CFunction
    public static native int kill(int pid, int sig);

    @CFunction
    public static native int sigprocmask(int how, sigset_tPointer set, sigset_tPointer oldset);

    /** A pointer to a signal set. The implementation of a signal set is platform-specific. */
    @CPointerTo(nameOfCType = "sigset_t")
    public interface sigset_tPointer extends PointerBase {
    }

    public interface SignalDispatcher extends CFunctionPointer {

        /** From signal(2): typedef void (*sig_t) (int). */
        @InvokeCFunctionPointer
        void dispatch(int sig);
    }

    @CFunction
    public static native SignalDispatcher signal(int signum, SignalDispatcher handler);

    @CConstant
    public static native SignalDispatcher SIG_DFL();

    @CConstant
    public static native SignalDispatcher SIG_IGN();

    @CConstant
    public static native SignalDispatcher SIG_ERR();

    @CFunction
    public static native int raise(int signum);

    @CStruct
    public interface siginfo_t extends PointerBase {
        /* Fields unused */
    }

    @Platforms(InternalPlatform.LINUX_JNI_AND_SUBSTITUTIONS.class)
    @CPointerTo(nameOfCType = "long long int")
    public interface GregsPointer extends PointerBase {
        long read(int index);
    }

    @Platforms({DeprecatedPlatform.LINUX_SUBSTITUTION_AMD64.class, Platform.LINUX_AMD64.class})
    @CEnum
    @CContext(PosixDirectives.class)
    public enum GregEnum {
        REG_R8,
        REG_R9,
        REG_R10,
        REG_R11,
        REG_R12,
        REG_R13,
        REG_R14,
        REG_R15,
        REG_RDI,
        REG_RSI,
        REG_RBP,
        REG_RBX,
        REG_RDX,
        REG_RAX,
        REG_RCX,
        REG_RSP,
        REG_RIP,
        REG_EFL,
        REG_CSGSFS,
        REG_ERR,
        REG_TRAPNO,
        REG_OLDMASK,
        REG_CR2;

        @CEnumValue
        public native int getCValue();
    }

    @Platforms({InternalPlatform.LINUX_JNI_AND_SUBSTITUTIONS.class, InternalPlatform.DARWIN_JNI_AND_SUBSTITUTIONS.class})
    @CStruct
    public interface ucontext_t extends PointerBase {
        /*-
            // AMD64 userlevel context.
            typedef struct ucontext
              {
                unsigned long int uc_flags;
                struct ucontext *uc_link;
                stack_t uc_stack;
                mcontext_t uc_mcontext;
                __sigset_t uc_sigmask;
                struct _libc_fpstate __fpregs_mem;
              } ucontext_t;
        
            // Context to describe whole processor state.
            typedef struct
              {
                gregset_t gregs;
                // Note that fpregs is a pointer.
                fpregset_t fpregs;
                __extension__ unsigned long long __reserved1 [8];
            } mcontext_t;
         */
        @CFieldAddress("uc_mcontext.gregs")
        @Platforms({DeprecatedPlatform.LINUX_SUBSTITUTION_AMD64.class, Platform.LINUX_AMD64.class})
        GregsPointer uc_mcontext_gregs();

        /*-
        // AArch64 userlevel context.
        typedef struct ucontext
        {
            unsigned long uc_flags;
            struct ucontext *uc_link;
            stack_t uc_stack;
            __sigset_t uc_sigmask;
            mcontext_t uc_mcontext;
        } ucontext_t;
        typedef struct sigcontext mcontext_t;
        struct sigcontext {
                __u64 fault_address;
                // AArch64 registers
                __u64 regs[31];
                __u64 sp;
                __u64 pc;
                __u64 pstate;
                // 4K reserved for FP/SIMD state and future expansion
                __u8 __reserved[4096] __attribute__((__aligned__(16)));
        };
        */
        @CFieldAddress("uc_mcontext")
        @Platforms({DeprecatedPlatform.LINUX_SUBSTITUTION_AARCH64.class, Platform.LINUX_AARCH64.class})
        mcontext_t uc_mcontext();

        @CField("uc_mcontext")
        @Platforms({DeprecatedPlatform.DARWIN_SUBSTITUTION_AMD64.class, Platform.DARWIN_AMD64.class})
        MContext64 uc_mcontext64();

    }

    @Platforms({DeprecatedPlatform.DARWIN_SUBSTITUTION_AMD64.class, Platform.DARWIN_AMD64.class})
    @CStruct(value = "__darwin_mcontext64", addStructKeyword = true)
    public interface MContext64 extends PointerBase {

        @CFieldOffset("__ss.__rax")
        int rax_offset();

        @CFieldOffset("__ss.__rbx")
        int rbx_offset();

        @CFieldOffset("__ss.__rip")
        int rip_offset();

        @CFieldOffset("__ss.__rsp")
        int rsp_offset();

        @CFieldOffset("__ss.__rcx")
        int rcx_offset();

        @CFieldOffset("__ss.__rdx")
        int rdx_offset();

        @CFieldOffset("__ss.__rbp")
        int rbp_offset();

        @CFieldOffset("__ss.__rsi")
        int rsi_offset();

        @CFieldOffset("__ss.__rdi")
        int rdi_offset();

        @CFieldOffset("__ss.__r8")
        int r8_offset();

        @CFieldOffset("__ss.__r9")
        int r9_offset();

        @CFieldOffset("__ss.__r10")
        int r10_offset();

        @CFieldOffset("__ss.__r11")
        int r11_offset();

        @CFieldOffset("__ss.__r12")
        int r12_offset();

        @CFieldOffset("__ss.__r13")
        int r13_offset();

        @CFieldOffset("__ss.__r14")
        int r14_offset();

        @CFieldOffset("__ss.__r15")
        int r15_offset();

        @CFieldOffset("__ss.__rflags")
        int efl_offset();
    }

    @CStruct
    @Platforms({DeprecatedPlatform.LINUX_SUBSTITUTION_AARCH64.class, Platform.LINUX_AARCH64.class})
    public interface mcontext_t extends PointerBase {
        @CField
        long fault_address();

        @CFieldAddress
        GregsPointer regs();

        @CField
        long sp();

        @CField
        long pc();

        @CField
        long pstate();
    }

    public interface AdvancedSignalDispatcher extends CFunctionPointer {

        /** From SIGACTION(2): void (*sa_sigaction)(int, siginfo_t *, void *). */
        @InvokeCFunctionPointer
        void dispatch(int signum, siginfo_t siginfo, WordPointer opaque);
    }

    @Platforms({InternalPlatform.LINUX_JNI_AND_SUBSTITUTIONS.class, InternalPlatform.DARWIN_JNI_AND_SUBSTITUTIONS.class})
    @CConstant
    public static native int SA_SIGINFO();

    @CStruct(addStructKeyword = true)
    public interface sigaction extends PointerBase {
        /*-
           struct sigaction {
               void     (*sa_handler)(int);
               void     (*sa_sigaction)(int, siginfo_t *, void *);
               sigset_t   sa_mask;
               int        sa_flags;
               void     (*sa_restorer)(void);
           };
         */

        @CField
        SignalDispatcher sa_handler();

        @CField
        void sa_handler(SignalDispatcher value);

        @CField
        AdvancedSignalDispatcher sa_sigaction();

        @CField
        void sa_sigaction(AdvancedSignalDispatcher value);

        @CField
        int sa_flags();

        @CField
        void sa_flags(int value);

        @CFieldAddress
        sigset_tPointer sa_mask();
    }

    @CFunction
    public static native int sigaction(SignalEnum signum, sigaction act, sigaction oldact);

    @CEnum
    @CContext(PosixDirectives.class)
    public enum SignalEnum {
        SIGABRT,
        SIGALRM,
        SIGBUS,
        SIGCHLD,
        SIGCONT,
        SIGFPE,
        SIGHUP,
        SIGILL,
        SIGINT,
        SIGIO,
        SIGIOT,
        SIGKILL,
        SIGPIPE,
        SIGPROF,
        SIGQUIT,
        SIGSEGV,
        SIGSTOP,
        SIGSYS,
        SIGTERM,
        SIGTRAP,
        SIGTSTP,
        SIGTTIN,
        SIGTTOU,
        SIGURG,
        SIGUSR1,
        SIGUSR2,
        SIGVTALRM,
        SIGWINCH,
        SIGXCPU,
        SIGXFSZ;

        @CEnumValue
        public native int getCValue();
    }

    @Platforms(InternalPlatform.LINUX_JNI_AND_SUBSTITUTIONS.class)
    @CEnum
    @CContext(PosixDirectives.class)
    public enum LinuxSignalEnum {
        SIGPOLL,
        SIGPWR;

        @CEnumValue
        public native int getCValue();
    }

    @Platforms(InternalPlatform.DARWIN_JNI_AND_SUBSTITUTIONS.class)
    @CEnum
    @CContext(PosixDirectives.class)
    public enum DarwinSignalEnum {
        SIGINFO,
        SIGEMT;

        @CEnumValue
        public native int getCValue();
    }

    @CFunction
    public static native int sigemptyset(sigset_tPointer set);

    /* Allow lower-case type names, underscores in names, etc.: Checkstyle: resume. */
}
