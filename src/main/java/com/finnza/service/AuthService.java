package com.finnza.service;

import com.finnza.domain.entity.Usuario;
import com.finnza.dto.request.LoginRequest;
import com.finnza.dto.response.LoginResponse;
import com.finnza.dto.response.UsuarioDTO;
import com.finnza.repository.UsuarioRepository;
import com.finnza.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de autenticação
 */
@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Realiza o login do usuário
     */
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String token = tokenProvider.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmailWithPermissoes(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

            // Atualiza último acesso
            usuario.atualizarUltimoAcesso();
            usuarioRepository.save(usuario);

            return LoginResponse.builder()
                    .token(token)
                    .usuario(UsuarioDTO.fromEntity(usuario))
                    .build();

        } catch (Exception e) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }
    }
}

