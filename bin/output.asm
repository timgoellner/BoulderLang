global _start
_start:
    mov rax, 23
    push rax
    push QWORD [rsp + 0]
    mov rax, 30
    push rax
    push QWORD [rsp + 0]
    mov rax, 60
    pop rdi
    syscall
    mov rax, 60
    mov rdi, 0
    syscall
