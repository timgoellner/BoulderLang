section .text
    global _start
_start:
    mov rax, 3
    push rax
    mov rax, 2
    push rax
    pop rax
    pop rbx
    mul rbx
    push rax
    mov rax, 10
    push rax
    pop rax
    pop rbx
    add rax, rbx
    push rax
    mov rax, 4
    push rax
    push QWORD [rsp + 8]
    mov rax, 4
    push rax
    pop rax
    pop rbx
    add rax, rbx
    push rax
    pop rax
    pop rbx
    div rbx
    push rax
    mov rax, 0
    push rax
    mov rax, 0
    push rax
    mov rax, 12
    push rax
    push QWORD [rsp + 24]
    pop rax
    pop rbx
    cmp rax, rbx
    jle l0True
    mov rax, 0
    jmp l0End
l0True:
    mov rax, 1
l0End:
    push rax
    pop rax
    pop rbx
    or rax, rbx
    cmp rax, 1
    je l1True
    mov rax, 0
    jmp l1End
l1True:
    mov rax, 1
l1End:
    push rax
    add rsp, 0
    mov rax, 20
    push rax
    mov rax, 2
    push rax
    mov rax, 22
    push rax
    pop rax
    pop rbx
    sub rax, rbx
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    cmp rax, rbx
    jge l2True
    mov rax, 0
    jmp l2End
l2True:
    mov rax, 1
l2End:
    push rax
    pop rax
    cmp rax, 0
    je l3True
    mov rax, 0
    jmp l3End
l3True:
    mov rax, 1
l3End:
    push rax
    pop rax
    cmp rax, 1
    je l4True
    jmp l4False
l4True:
    mov rax, 32
    push rax
    mov rax, 32
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    cmp rax, rbx
    je l5True
    mov rax, 0
    jmp l5End
l5True:
    mov rax, 1
l5End:
    push rax
    pop rax
    cmp rax, 1
    je l6True
    jmp l6False
l6True:
    push QWORD [rsp + 0]
    mov rax, 60
    pop rdi
    syscall
    jmp l6End
l6False:
l6End:
    add rsp, 8
    jmp l4End
l4False:
    mov rax, 1
    push rax
    mov rax, 1
    push rax
    pop rax
    pop rbx
    cmp rax, rbx
    je l7True
    mov rax, 0
    jmp l7End
l7True:
    mov rax, 1
l7End:
    push rax
    pop rax
    cmp rax, 1
    je l8True
    jmp l8False
l8True:
    mov rax, 2
    push rax
    jmp l8End
l8False:
l8End:
    add rsp, 0
l4End:
    mov rax, 5
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    add rax, rbx
    push rax
    mov rax, 60
    pop rdi
    syscall
    mov rax, 60
    mov rdi, 0
    syscall
