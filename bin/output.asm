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
    jmp l0False
l0True:
    mov rax, 1
    jmp l0End
l0False:
    mov rax, 0
l0End:
    push rax
    pop rax
    pop rbx
    or rax, rbx
    cmp rax, 1
    je l1True
    jmp l1False
l1True:
    mov rax, 1
    jmp l1End
l1False:
    mov rax, 0
l1End:
    push rax
    add rsp, 0
    mov rax, 0
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    and rax, rbx
    cmp rax, 1
    je l2True
    jmp l2False
l2True:
    mov rax, 1
    jmp l2End
l2False:
    mov rax, 0
l2End:
    push rax
    mov rax, 60
    pop rdi
    syscall
    mov rax, 60
    mov rdi, 0
    syscall
