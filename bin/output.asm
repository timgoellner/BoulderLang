global _start
_start:
    mov rax, 213
    push rax
    mov rax, 32
    push rax
    add rsp, 0
    mov rax, 4
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    add rax, rbx
    push rax
    push QWORD [rsp + 0]
    mov rax, 60
    pop rdi
    syscall
    mov rax, 60
    mov rdi, 0
    syscall
