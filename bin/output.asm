section .text
    global _start
_start:
    ; StatementSet
    mov rax, 9
    push rax
    ; StatementSet
    mov rax, 9
    push rax
    ; Loop
l0Start:
    mov rax, 0
    push rax
    push QWORD [rsp + 16]
    pop rax
    pop rbx
    cmp rax, rbx
    jge l1True
    mov rax, 0
    jmp l1End
l1True:
    mov rax, 1
l1End:
    push rax
    pop rax
    cmp rax, 1
    jne l0End
    ; Loop
l2Start:
    mov rax, 0
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    cmp rax, rbx
    jge l3True
    mov rax, 0
    jmp l3End
l3True:
    mov rax, 1
l3End:
    push rax
    pop rax
    cmp rax, 1
    jne l2End
    ; StatementPrint
    push QWORD [rsp + 0]
    pop rdx
    push rdx
    add rdx, 48
    push rdx
    mov rsi, rsp
    mov rax, 1
    mov edi, 1
    mov rdx, 1
    syscall
    add rsp, 16
    ; StatementAssignment
    mov rax, 1
    push rax
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    sub rax, rbx
    push rax
    pop rax
    add rsp, 8
    push rax
    sub rsp, 0
    add rsp, 0
    jmp l2Start
l2End:
    ; StatementAssignment
    mov rax, 1
    push rax
    push QWORD [rsp + 16]
    pop rax
    pop rbx
    sub rax, rbx
    push rax
    pop rax
    add rsp, 16
    push rax
    sub rsp, 8
    ; StatementAssignment
    mov rax, 9
    push rax
    pop rax
    add rsp, 8
    push rax
    sub rsp, 0
    add rsp, 0
    jmp l0Start
l0End:
    mov rax, 60
    mov rdi, 0
    syscall
