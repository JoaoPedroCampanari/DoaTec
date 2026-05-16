package com.doatec.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integração - Upload de Arquivo")
class FileUploadIntegrationTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/upload/foto")
    class UploadFotoTests {

        @Test
        @DisplayName("upload de imagem JPEG válida retorna 200")
        void uploadJpegValido_retorna200() throws Exception {
            // Criar um arquivo JPEG fake com magic bytes corretos
            byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00};
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", jpegBytes);

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.startsWith("/uploads/")));
        }

        @Test
        @DisplayName("upload de imagem PNG válida retorna 200")
        void uploadPngValido_retorna200() throws Exception {
            // Criar um arquivo PNG fake com magic bytes corretos
            byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x00, 0x00, 0x00, 0x00};
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.png", "image/png", pngBytes);

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.startsWith("/uploads/")));
        }

        @Test
        @DisplayName("upload de arquivo sem content-type retorna 400")
        void uploadSemContentType_retorna400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", null, new byte[]{0x00});

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Arquivo deve ser uma imagem."));
        }

        @Test
        @DisplayName("upload de arquivo não-imagem retorna 400")
        void uploadNaoImagem_retorna400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "documento.pdf", "application/pdf", new byte[]{0x00});

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Arquivo deve ser uma imagem."));
        }

        @Test
        @DisplayName("upload de arquivo com magic bytes incorretos retorna 400")
        void uploadMagicBytesIncorretos_retorna400() throws Exception {
            // Content-type diz JPEG, mas magic bytes são de PNG
            byte[] wrongBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x00, 0x00};
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", wrongBytes);

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Assinatura do arquivo não corresponde ao tipo declarado."));
        }

        @Test
        @DisplayName("upload de arquivo muito grande retorna 400")
        void uploadArquivoGrande_retorna400() throws Exception {
            // Criar arquivo de 6MB (maior que o limite de 5MB)
            byte[] bigBytes = new byte[6 * 1024 * 1024];
            bigBytes[0] = (byte) 0xFF;
            bigBytes[1] = (byte) 0xD8;
            bigBytes[2] = (byte) 0xFF;
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", bigBytes);

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Arquivo excede o tamanho máximo de 5MB."));
        }

        @Test
        @DisplayName("upload de arquivo GIF válida retorna 200")
        void uploadGifValido_retorna200() throws Exception {
            byte[] gifBytes = new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x00, 0x00};
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.gif", "image/gif", gifBytes);

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.startsWith("/uploads/")));
        }

        @Test
        @DisplayName("upload de arquivo WebP válida retorna 200")
        void uploadWebpValido_retorna200() throws Exception {
            // RIFF header para WebP
            byte[] webpBytes = new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00};
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.webp", "image/webp", webpBytes);

            mockMvc.perform(multipart("/api/upload/foto")
                            .file(file)
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.startsWith("/uploads/")));
        }
    }
}
