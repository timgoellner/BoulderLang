section .text
    global _start
_start:
    ; StatementSet
    mov rax, 0
    push rax
    ; StatementSet
    mov rax, 1
    push rax
    ; StatementSet
    mov rax, 12
    push rax
    ; StatementSet
    mov rax, 0
    push rax
    ; Loop
l0Start:
    push QWORD [rsp + 8]
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    cmp rax, rbx
    jl l1True
    mov rax, 0
    jmp l1End
l1True:
    mov rax, 1
l1End:
    push rax
    pop rax
    cmp rax, 1
    jne l0End
    ; StatementSet
    push QWORD [rsp + 24]
    ; StatementAssignment
    push QWORD [rsp + 24]
    pop rax
    add rsp, 40
    push rax
    sub rsp, 32
    ; StatementAssignment
    push QWORD [rsp + 24]
    push QWORD [rsp + 8]
    pop rax
    pop rbx
    add rax, rbx
    push rax
    pop rax
    add rsp, 32
    push rax
    sub rsp, 24
    ; StatementAssignment
    mov rax, 1
    push rax
    push QWORD [rsp + 16]
    pop rax
    pop rbx
    add rax, rbx
    push rax
    pop rax
    add rsp, 16
    push rax
    sub rsp, 8
    ; StatementPrint
    push QWORD [rsp + 32]
    mov bl, 10
    pop rax
    push 0
    push 10
l2Convert:
    div bl
    mov dl, ah
    add dl, 48
    push rdx
    xor ah, ah
    cmp al, 0
    jnz l2Convert
l2Print:
    mov rsi, rsp
    mov rax, 1
    mov edi, 1
    mov rdx, 1
    syscall
    pop rdx
    cmp dx, 0
    jnz l2Print
    add rsp, 8
    jmp l0Start
l0End:
    mov rax, 60
    mov rdi, 0
    syscall
