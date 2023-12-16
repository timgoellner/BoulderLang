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
    cmp rax, 1
    je l3True
    jmp l3End
l3True:
    mov rax, 32
    push rax
    mov rax, 32
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    cmp rax, rbx
    je l4True
    mov rax, 0
    jmp l4End
l4True:
    mov rax, 1
l4End:
    push rax
    pop rax
    cmp rax, 1
    je l5True
    jmp l5End
l5True:
    push QWORD [rsp + 0]
    mov rax, 60
    pop rdi
    syscall
l5End:
    add rsp, 8
l3End:
    mov rax, 1
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    and rax, rbx
    cmp rax, 1
    je l6True
    mov rax, 0
    jmp l6End
l6True:
    mov rax, 1
l6End:
    push rax
    mov rax, 60
    pop rdi
    syscall
    mov rax, 60
    mov rdi, 0
    syscall
